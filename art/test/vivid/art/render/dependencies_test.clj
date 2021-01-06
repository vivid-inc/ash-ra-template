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

(ns vivid.art.render.dependencies-test
  (:require
    [clojure.test :refer :all]
    [vivid.art :as art]))

(deftest clojure-versions
  (are [version-string]
    (= version-string
         (art/render "<%= (let [{:keys [major minor incremental]} *clojure-version*]
(format \"%d.%d.%d\" major minor incremental))%>"
                     {:dependencies {'org.clojure/clojure {:mvn/version version-string}}}))
    "1.9.0"
    "1.10.0"
    "1.10.1"))
