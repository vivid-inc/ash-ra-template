; Copyright 2020 Vivid Inc.
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;    https://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns vivid.art.cli.test-lib
  (:require
    [special.core :as special])
  (:import
    (clojure.lang ExceptionInfo)))

(defn special-unwind-on-signal [f signal]
  "Runs function f using special/manage. If and when special signal occurs,
  unwinds the stack, returning the value that was provided to the signal
  handler."
  (try
    ((special/manage
       f
       signal (fn [data]
                (throw (ex-info "" {:data data}))           ; Unwind the stack ..
                )))
    (catch ExceptionInfo ex
      (:data (ex-data ex))                                  ; .. and return the signal data.
      )))
