; Copyright 2022 Vivid Inc. and/or its affiliates.
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

(ns vivid.art.cli.watch
  (:require
    [nextjournal.beholder :as beholder]
    [vivid.art.cli.log :as log])
  (:import
    (java.io File)))

(defn watch-on-batches
      "Watches template paths in all of the supplied batches.
      Whenever a file system event occurs, calls event-fn
      with batch and event."
      [batches event-fn]
      ; TODO Configurable, default 50ms cool-down + event coalescing.
      ; TODO Document ^c to exit.
      (doseq [b batches]
             (doseq [^File f (:templates b)]
                    (let [t (.toString f)]
                         (log/*info-fn* "Watching" t)
                         (beholder/watch (partial event-fn b) t))))
      ; TODO For testing: (<!! wait-for-exit) (put! wait-for-exit :exit)
      (while true
             (Thread/sleep Long/MAX_VALUE)))
