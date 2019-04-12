; Copyright 2019 Vivid Inc.

(ns vivid.art.core
  (:require
    [clojure.string]
    [reduce-fsm :as fsm]
    [vivid.art.embed :as embed]))

; Referencing the canonical implementation of ERB: https://github.com/ruby/ruby/blob/trunk/lib/erb.rb

(defn lex
  "Tokenizes the template"
  [^String template-str]
  ; Note: Using regexes, I haven't figured out how to isolate both <% and <%=
  ; in the time I allotted myself. For the meantime, use the following hack:
  ; A regex splits <%= ; into a sequence of <% followed by = .
  ; Next, that sequence is collapsed into a single element in the stream.
  (let [parts (clojure.string/split template-str #"(?=<%=?)|(?<=<%=?)|(?=%>)|(?<=%>)")]
    (reduce (fn [xs x]
              (if (and (= (last xs) "<%")
                       (= x "="))
                (concat (butlast xs) ["<%="])
                (concat xs [x])))
            []
            parts)))

(defn echo
  "Echoes the value literal to the rendered output"
  [acc val & _]
  (let [escaped (clojure.string/escape val {\" "\\\""})]
    (assoc acc :output
               (conj (acc :output)
                     (str "(emit \"" escaped "\")")))))

(defn echo-eval
  "Echoes the result of evaluating the expression to the rendered output"
  [acc expr & _]
  (assoc acc :output
             (conj (acc :output)
                   (str "(emit " expr " )"))))

(defn -eval
  "Evaluates the expression to the rendered output"
  [acc expr & _]
  (assoc acc :output
             (conj (acc :output)
                   expr)))

(fsm/defsm tokens->forms
           [[:echo
             "<%" -> :eval
             "<%=" -> :echo-eval
             _ -> {:action echo} :echo]
            [:eval
             "%>" -> :echo
             _ -> {:action -eval} :eval]
            [:echo-eval
             "%>" -> :echo
             _ -> {:action echo-eval} :echo-eval]]
           :default-acc {:output []})

(defn parse
  "Parses a sequence of tokens into Clojure code suitable for
  evaluating to produce the template output."
  [tokens]
  (let [fsm-result (tokens->forms tokens)]
    (fsm-result :output)))

(defn wrap-forms [forms]
  (concat ["(def ^StringBuilder __vt__art__sb__emit (new StringBuilder))"
           "(defn emit [val] (.append __vt__art__sb__emit val))"]
          forms
          ["(.toString __vt__art__sb__emit)"]))

(defn evaluate [forms]
  (let [wrapped-forms (clojure.string/join "\n" (wrap-forms forms))]
    (embed/eval-in-one-shot-runtime wrapped-forms)))

(defn render
  "Renders an input string containing Ash-Ra Template
  -formatted content to an output string"
  [^String input]
  (-> input
      (lex)
      (parse)
      (evaluate)))
