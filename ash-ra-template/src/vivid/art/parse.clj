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
  (update-in acc [:output] conj (str "(emit " expr " )")))

(defn -eval
  "Evaluates the expression to the rendered output"
  [acc expr & _]
  (update-in acc [:output] conj expr))

; TODO Explicitly test all combinations (DFA state transitions), in part to clarify the rules.
(fsm/defsm tokens->forms
           [[:echo
             :begin-forms -> :eval
             :begin-eval -> :echo-eval
             :end-eval -> :echo
             :end-forms -> :echo
             _ -> {:action echo} :echo]
            [:eval
             :end-eval -> :echo
             :end-forms -> :echo
             _ -> {:action -eval} :eval]
            [:echo-eval
             :end-eval -> :echo
             :end-forms -> :echo
             _ -> {:action echo-eval} :echo-eval]]
           :default-acc {:output []})

(defn parse
  "Parses a sequence of tokens into Clojure code that, when evaluated, produces the template output."
  [tokens]
  (let [fsm-result (tokens->forms tokens)]
    (fsm-result :output)))
