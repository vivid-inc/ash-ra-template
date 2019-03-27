(ns vivid.art.core
  (:require
    [clojure.string]
    [eval-soup.core :as eval-soup]
    [reduce-fsm :as fsm]))

; Referencing the canonical implementation of ERB: https://github.com/ruby/ruby/blob/trunk/lib/erb.rb

(defn lex
  "Tokenizes the input string"
  [str]
  ; Note: I haven't figured out how to isolate both <% and <%= in the
  ; time I allotted myself.
  ;
  ; For the meantime, we use the following hack:
  ; A regex splits <%= ; into a sequence of <% followed by = .
  ; Next, that sequence is collapsed into a single element in the stream.
  (let [ts (clojure.string/split str #"(?=<%=?)|(?<=<%=?)|(?=%>)|(?<=%>)")]
    (reduce (fn [xs x]
              (if (and (= (last xs) "<%")
                       (= x "="))
                (concat (butlast xs) ["<%="])
                (concat xs [x])))
            []
            ts)))

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

(defn parse [tokens]
  (let [fsm-result (tokens->forms tokens)]
    (fsm-result :output)))

(defn wrap-forms [forms]
  (concat ["(def ^StringBuilder __vt__art__sb__emit (new StringBuilder))"
           "(defn emit [val] (.append __vt__art__sb__emit val))"]
          forms
          ["(.toString __vt__art__sb__emit)"]))

(defn render [input]
  "Renders an input string containing Ash-Ra Template formatted content to an output string"
  (-> input
      (lex)
      (parse)
      (wrap-forms)
      (eval-soup/code->results)
      (last)))
