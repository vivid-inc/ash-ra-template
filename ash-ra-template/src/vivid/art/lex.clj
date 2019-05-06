; Copyright 2019 Vivid Inc.

(ns vivid.art.lex
  (:require
    [clojure.set :refer [difference]])
  (:import
    (java.io StringReader)))

; Delimiter Trie
;
; Using vivid.art.delimiters/erb as an example:
;
;     {"<%"  :begin-form
;      "%>"  :end-form
;      "<%=" :begin-emit}
;
; the trie data structure is then:
;
;     {:path ""
;      \<    {:path "<"
;             \%    {:token :begin-form
;                    \=     {:token :begin-emit}}}
;      \%    {:path "%"
;             \>    {:token :end-form}}}
;
; Each delimiter :token is accessible at the path formed by the character
; array representation of its delimiter string.
; All trie nodes know their own :path in string form (not as a char array);
; path is dumped to the buffer whenever only a partial delimiter match was made.

(defn string->char-seq
  "Creates a sequence of characters from the string str."
  [str]
  (seq (char-array str)))

(defn delimiters->char-seqs
  "Converts delimiter strings to their character sequence representations."
  [delimiters]
  (reduce-kv #(assoc %1 (string->char-seq %2) %3) {} delimiters))

(defn add-token-to-trie
  "Adds a token at path cs to trie."
  [trie cs token]
  (assoc-in trie `(~@cs :token) token))

(defn add-path-to-trie [t path]
  (assoc-in t `(~@(string->char-seq path) :path) path))

(defn key-paths
  "Produces a set of paths, one for each node in the Trie."
  ([t] (set (key-paths t "")))
  ([t prefix]
   (reduce
     (fn [memo [k node]]
       (if (map? node)
         (concat memo [prefix] (key-paths node (str prefix k)))
         (conj memo prefix)))
     [] t)))

(defn trie [delimiters]
  (let [t (reduce-kv add-token-to-trie {} (delimiters->char-seqs delimiters))
        paths (difference (key-paths t) (set (keys delimiters)))]
    ; Mark all other nodes with their path in the trie.
    (reduce add-path-to-trie t paths)))

;
; Lex
;

(defn eos?
  "Does this character indicate the end of a Java Reader stream?"
  [c]
  (= c -1))

(defn remaining
  [x]
  (when (seq x)
    [x]))

(defn trie-step [trie c]
  (when-not (eos? c)
    (get trie (char c))))

(defn lex
  ; TODO validate input delimiters
  "Tokenize a template into a sequence of lexemes, suitable for parsing.

  Each lexeme is any of:
  - Template content, as a string.
  - Template delimiter, as a Clojure keyword."
  [^String template-str
   delimiters]
  ; Hand-built eager LL(1) lexer.
  (let [root-trie-node (trie delimiters)
        in (StringReader. template-str)]
    (loop [out []
           c (.read in)
           n (trie-step root-trie-node c)
           buf (StringBuilder.)]

      (cond
        ; Flush remaining buffers at end-of-stream.
        (eos? c)
        (let [remaining (str buf)]
          (concat out (when (seq remaining)
                        [remaining])))

        ; The current charater sequence matches part or all of a known delimiter.
        n
        (let [look-ahead-c (.read in)
              peek-n (trie-step n look-ahead-c)]
          (cond
            ; Looking ahead one more character still matches part or all of a known delimiter.
            peek-n (recur out
                        look-ahead-c
                        peek-n
                        buf)
            ; Looking ahead did not reveal a match, but the current set of characters is a delimiter.
            ; Emit its token.
            (get n :token) (recur (concat out (remaining (str buf)) [(get n :token)])
                                  look-ahead-c
                                  (trie-step root-trie-node look-ahead-c)
                                  (StringBuilder.))
            ; A full match will not be produced; flush the buffer and the partial match.
            :else (recur out
                         look-ahead-c
                         (trie-step root-trie-node look-ahead-c)
                         (.append buf (get n :path)))))

        ; Read the next character and attempt to match a delimiter anew.
        :else
        (let [next-c (.read in)]
          (recur out
                 next-c
                 (trie-step root-trie-node next-c)
                 (.append buf (char c))))))))
