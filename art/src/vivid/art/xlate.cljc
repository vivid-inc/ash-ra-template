; Copyright 2023 Vivid Inc. and/or its affiliates.
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     https://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns ^:internal-api vivid.art.xlate
  (:refer-clojure :exclude [eval])
  (:require
   [clojure.string]
   [tilakone.core :as tk]))

(defn echo
  "Writes an (emit) to the compiled code that echoes the plain string but with escaping."
  [acc val]
  (let [escaped (clojure.string/escape val {\" "\\\""
                                            \\ "\\\\"})]
    (conj acc (str "(emit \"" escaped "\")"))))

(defn eval
  "Writes an (emit) to the compiled code that outputs the result of evaluating the forms."
  [acc expr]
  (conj acc (str "(emit " expr " )")))

(defn forms
  "Echoes Clojure forms from the template to the compiled code."
  [acc expr]
  (conj acc expr))

(def lenient-fsm-states
  "Lenient in that this FSM will accept :end-* tokens while already in the end state of :echo."
  [{::tk/name :echo
    ::tk/transitions [{::tk/on :vivid.art/begin-eval   ::tk/to :eval}
                      {::tk/on :vivid.art/begin-forms  ::tk/to :forms}
                      {::tk/on :vivid.art/end-eval     ::tk/to :echo}
                      {::tk/on :vivid.art/end-forms    ::tk/to :echo}
                      {::tk/on ::tk/_                  ::tk/actions [echo]}]}
   {::tk/name :eval
    ::tk/transitions [{::tk/on :vivid.art/end-eval     ::tk/to :echo}
                      {::tk/on :vivid.art/end-forms    ::tk/to :echo}
                      {::tk/on ::tk/_                  ::tk/actions [eval]}]}
   {::tk/name :forms
    ::tk/transitions [{::tk/on :vivid.art/end-eval     ::tk/to :echo}
                      {::tk/on :vivid.art/end-forms    ::tk/to :echo}
                      {::tk/on ::tk/_                  ::tk/actions [forms]}]}])

(def lenient-fsm
  {::tk/states lenient-fsm-states
   ::tk/action! (fn [{::tk/keys [action signal] :as fsm}]
                  (update-in fsm [::tk/process :output] action signal))
   ::tk/state   :echo
   :output      []})

(defn translate
  "Translates a sequence of tokens into Clojure code that,
  when evaluated, produces the template output."
  [token-stream]
  (let [result (reduce tk/apply-signal lenient-fsm token-stream)]
      (:output result)))
