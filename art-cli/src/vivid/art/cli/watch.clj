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

(ns ^:internal-api vivid.art.cli.watch
  (:require
   [nextjournal.beholder :as beholder]
   [vivid.art.cli.log :as log])
  (:import
   (java.io File)))

; TODO De-bounce / coalesce writes, on each input file.
; TODO Watch all elements in directories.
; https://stackoverflow.com/questions/35663415/throttle-functions-with-core-async
; https://ericnormand.me/guide/clojure-concurrency
#_(defn debounce [file]
    (let [out (chan)]
      (go-loop [last-val nil]
               (let [val (if (nil? last-val) (<! in) last-val)
                     timer (timeout 50)
                     [new-val ch] (alts! [in timer])]
                 (condp = ch
                   timer (do (>! out val) (recur nil))
                   in    (recur new-val))))
      out))

(defn watch-on-batches
  "Watches template paths in all supplied batches.
      Whenever a file system event occurs, calls event-fn
      with batch and event."
  [batches event-fn]
  (doseq [b batches]
    (doseq [^File f (:templates b)]
      (let [t (.toString f)]
        (log/*info-fn* "Watching" t)
        (beholder/watch (partial event-fn b) t))))
      ; TODO For testing: (<!! wait-for-exit) (put! wait-for-exit :exit)
  (while true
    (Thread/sleep Long/MAX_VALUE)))
