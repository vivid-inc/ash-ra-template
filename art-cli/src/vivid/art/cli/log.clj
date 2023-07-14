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

(ns vivid.art.cli.log
  "Logging facility used by this library. Consumers of this library may bind
  the logging fn vars to their own functions.")

(def mutex (Object.))

(defn- p [level args]
  (let [line (str "ART " (name level) ": "
                  (apply pr-str args))]
    (locking mutex
      (println line))))

(defn ^:dynamic *debug-fn*
  [& args]
  (p :debug args))

(defn ^:dynamic *info-fn*
  [& args]
  (p :info args))

(defn ^:dynamic *warn-fn*
  [& args]
  (p :warn args))
