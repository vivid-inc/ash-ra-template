; Copyright 2019 Vivid Inc.

(ns vivid.art.lex
  (:require
    [instaparse.core :as insta]))

(defn lex
  ; TODO validate input delimiters
  "Tokenize a template into a sequence of lexemes, suitable for parsing.

  Each lexeme is any of:
  - Template content, as a string.
  - Template delimiter, as a Clojure keyword."
  [^String template-str
   delimiters]
  (let [lenient-grammar "s = (begin-eval | begin-echo-eval | end | content)*
                         begin-eval = '<%'
                         begin-echo-eval = '<%='
                         end = '%>'
                         content = #'(?:(?!<%|<%=|%>).)*'"
        parser (insta/parser lenient-grammar)]
    (->> template-str
         (insta/parse parser)
         (insta/transform {:content         str
                           :begin-eval      (fn [_] :vivid.art/begin-eval)
                           :begin-echo-eval (fn [_] :vivid.art/begin-echo-eval)
                           :end             (fn [_] :vivid.art/end)})
         (rest)                                             ; TODO
         #_#(if (= :s (first %))
            (rest %)
            %))))
