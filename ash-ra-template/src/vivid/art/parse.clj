; Copyright 2019 Vivid Inc.

(ns vivid.art.parse
  (:require
    [clojure.string]
    [reduce-fsm :as fsm]))

(defn echo
  "Echoes the value literal to the rendered output"
  [acc val & _]
  (let [escaped (clojure.string/escape val {\" "\\\""})]
    (update-in acc [:output] conj (str "(emit \"" escaped "\")"))))

(defn echo-eval
  "Echoes the result of evaluating the expression to the rendered output"
  [acc expr & _]
  ; TODO Wrap expr with implicit (do)?
  (update-in acc [:output] conj (str "(emit " expr " )")))

(defn -eval
  "Evaluates the expression to the rendered output"
  [acc expr & _]
  (update-in acc [:output] conj expr))

; TODO Explicitly test all combinations (DFA state transitions), in part to clarify the rules.
(fsm/defsm lenient-parser
           [[:echo
             :vivid.art/begin-eval -> :eval
             :vivid.art/begin-echo-eval -> :echo-eval
             :vivid.art/end -> :echo
             _ -> {:action echo} :echo]
            [:eval
             :vivid.art/end -> :echo
             _ -> {:action -eval} :eval]
            [:echo-eval
             :vivid.art/end -> :echo
             _ -> {:action echo-eval} :echo-eval]]
           :default-acc {:output []})

(defn parse
  "Parses a sequence of tokens into Clojure code that, when evaluated, produces the template output."
  [tokens]
  (let [fsm-result (lenient-parser tokens)]
    (fsm-result :output)))
