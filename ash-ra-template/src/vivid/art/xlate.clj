; Copyright 2019 Vivid Inc.

(ns vivid.art.xlate
  (:refer-clojure :exclude [eval])
  (:require
    [clojure.string]
    [reduce-fsm :as fsm]))

(defn echo
  "Emits a Clojure form that echoes the value literal to the rendered output.
  The value literal could be either plain template text or Clojure forms."
  [acc val & _]
  (let [escaped (clojure.string/escape val {\" "\\\""})]
    (update-in acc [:output] conj (str "(emit \"" escaped "\")"))))

(defn echo-eval
  "Emits a Clojure form that echoes the result of evaluating the expression to the rendered output"
  [acc expr & _]
  ; TODO Wrap expr with implicit (do)?
  (update-in acc [:output] conj (str "(emit " expr " )")))

(defn eval
  "Emits a Clojure form that evaluates the expression to the rendered output"
  [acc expr & _]
  (update-in acc [:output] conj expr))

; TODO Explicitly test all combinations (DFA state transitions), in part to clarify the rules.
(fsm/defsm lenient-fsm
           [[:echo
             :vivid.art/begin-eval -> :eval
             :vivid.art/begin-echo-eval -> :echo-eval
             :vivid.art/end -> :echo
             _ -> {:action echo} :echo]
            [:eval
             :vivid.art/end -> :echo
             _ -> {:action eval} :eval]
            [:echo-eval
             :vivid.art/end -> :echo
             _ -> {:action echo-eval} :echo-eval]]
           :default-acc {:output []})

(defn translate
  "Translates a sequence of tokens into Clojure code that,
  when evaluated, produces the template output."
  [token-stream]
  (let [fsm-result (lenient-fsm token-stream)]
    (fsm-result :output)))
