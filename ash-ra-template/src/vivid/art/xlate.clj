; Copyright 2019 Vivid Inc.

(ns vivid.art.xlate
  (:refer-clojure :exclude [eval])
  (:require
    [clojure.string]
    [reduce-fsm :as fsm]))

(defn echo
  "Writes an (emit) to the compiled code that echoes the plain string but with escaping."
  [acc val & _]
  (let [escaped (clojure.string/escape val {\" "\\\""})]
    (update-in acc [:output] conj (str "(emit \"" escaped "\")"))))

(defn eval
  "Writes an (emit) to the compiled code that outputs the result of evaluating the forms."
  [acc expr & _]
  (update-in acc [:output] conj (str "(emit " expr " )")))

(defn forms
  "Echoes Clojure forms from the template to the compiled code."
  [acc expr & _]
  (update-in acc [:output] conj expr))

; TODO Explicitly test all combinations (DFA state transitions), in part to clarify the rules.
(fsm/defsm lenient-fsm
           [[:echo
             :vivid.art/begin-eval -> :eval
             :vivid.art/begin-forms -> :forms
             :vivid.art/end-eval -> :echo
             :vivid.art/end-forms -> :echo
             _ -> {:action echo} :echo]
            [:eval
             :vivid.art/end-eval -> :echo
             :vivid.art/end-forms -> :echo
             _ -> {:action eval} :eval]
            [:forms
             :vivid.art/end-eval -> :echo
             :vivid.art/end-forms -> :echo
             _ -> {:action forms} :forms]]
           :default-acc {:output []})

(defn translate
  "Translates a sequence of tokens into Clojure code that,
  when evaluated, produces the template output."
  [token-stream]
  (let [fsm-result (lenient-fsm token-stream)]
    (fsm-result :output)))
