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
  "Internal API."
  (:require
   [clojure.core.async :as a]))

; Implementation notes:
;
; Vertices:
; - tasks: Mapping from [f args] tuples to their individual raw channels.
; - raw-chan: Mapped from [f args]. Debounced function calls are effected by sending a message on the raw channel.
; - debounced-chan: Sources from raw-chan. Emits debounced messages on its own output channel.
;
; Edges, flow:
;   (debounce f args)
;       ↓
;   tasks mapping [f args] to raw-chan
;       ↓
;   messages on raw-chan debounced thru debounce-chan
;       ↓
;   debounced-call-loop listening on debounce-chan evaluates (apply f args)
;
; Purpose, behavior:
; Debounce function calls for any given function f with args with a timeout or delay.
; Each [f args] tuple is mapped in tasks to a raw channel.
; A flurry of identical debounce calls can be made each within successive timeout periods, however only the
; last call will be effected.

(def ^:const core-async-timeout-resolution
  "Sourced from
  core.async-1.6.681/src/main/clojure/clojure/core/async/impl/timers.clj
  def TIMEOUT_RESOLUTION_MS value."
  10)

(def tasks
  "A mapping from [f args] keys to raw channels for which callers send messages on
  Purely additive; no clean-up is performed."
  (atom {}))

(defn debounce-chan
  ([source timeout-ms]
   (debounce-chan (a/chan) source timeout-ms))
  ([c source timeout-ms]
   (a/go-loop [state ::init
               last-one nil
               cs [source]]
     (let [[_ threshold] cs
           [v sc] (a/alts! cs)]
       (condp = sc
         source (condp = state
                  ::init (recur ::debouncing
                                v
                                (conj cs (a/timeout timeout-ms)))
                  ::debouncing (recur state
                                      v
                                      (conj (pop cs) (a/timeout timeout-ms))))
         threshold (if last-one
                     (do (a/>! c last-one)
                         (recur ::init
                                nil
                                (pop cs)))
                     (recur ::init last-one (pop cs))))))
   c))

(defn debounced-call-loop [debounced-chan f args]
  (a/go-loop []
    (a/<! debounced-chan)
    (apply f args)
    (recur)))

(defn debounced-chan-for-call [timeout-ms f args]
  (let [key     [f args]
        _       (swap! tasks #(if (get % key)
                                ; raw-chan and hence the call loop are already setup.
                                %
                                ; Setup raw-chan and the call loop.
                                (let [raw-chan       (a/chan)
                                      debounced-chan (debounce-chan raw-chan timeout-ms)]
                                  (debounced-call-loop debounced-chan f args)
                                  (assoc % key raw-chan))))
        my-chan (get @tasks key)]
    my-chan))

(defn debounce
  "Calls a function f with args after a delay. Successive calls to the same [f args] tuple
  within the delay period are debounced, in effect being coalesced into a single call."
  [timeout-ms f & args]
  (let [raw-chan (debounced-chan-for-call timeout-ms f args)]
    (a/go
      (a/>! raw-chan ::pulse))))
