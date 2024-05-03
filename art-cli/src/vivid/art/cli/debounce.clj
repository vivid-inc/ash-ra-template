; Copyright 2024 Vivid Inc. and/or its affiliates.
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

(ns ^:internal-api vivid.art.cli.debounce
  (:require
   [clojure.core.async :as a :refer [<! >!]]))

(def ^:const core-async-timeout-resolution
  "Sourced from
  core.async-1.6.681/src/main/clojure/clojure/core/async/impl/timers.clj
  def TIMEOUT_RESOLUTION_MS value."
  10)

(def tasks
  "A mapping from {:f ... :args ...} keys to a/timeout values"
  (atom {}))
(def fire-ch (a/chan))

(defn debounce [timeout-ms f & args]
  (let [t (a/timeout timeout-ms)
        k {:f f :args args}]
    (swap! tasks assoc k t)
    (a/go
      (<! t)
      (>! fire-ch [k t]))))

(defn debounce-go-loop []
  (a/go-loop []
    (let [[k t-fired] (<! fire-ch)
          t-current   (get @tasks k)]
      (when (= t-fired t-current)

        ; TODO Will destroy new tasks registered concurrently with the execution of this fn.
        (swap! tasks dissoc k)

        ; TODO Don't allow for application to occur more than one at a time.
        (apply (:f k) (:args k)))
      (recur))))
