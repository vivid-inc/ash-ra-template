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

(ns vivid.art.cli.to-phase-test
  (:require
    [clojure.string]
    [clojure.test :refer :all]
    [vivid.art.cli.args]
    [vivid.art.cli.test-lib :refer [special-unwind-on-signal]]
    [vivid.art.cli.usage]
    [vivid.art.cli.validate :as validate]
    [vivid.art.specs]))

;
; CLI args
;

(deftest cli-all-known-phases
  (doseq [phase vivid.art.specs/render-phases]
    (let [args ["--to-phase" (name phase) "test-resources/empty.art"]
          res (vivid.art.cli.args/cli-args->batch args vivid.art.cli.usage/cli-options)]
      (is (= phase
             (:to-phase res))))))

(deftest cli-unknown-phases
  (doseq [phase [""
                 " "
                 "nonsense"]]
    (let [args ["--to-phase" phase "test-resources/empty.art"]
          f #(vivid.art.cli.args/cli-args->batch args vivid.art.cli.usage/cli-options)
          {:keys [step message]} (special-unwind-on-signal f :vivid.art.cli/error)]
      (is (and (= 'validate-to-phase step)
               (clojure.string/includes? message (str "'" phase "'")))))))


;
; Validators
;

(deftest public-api-unqualified-keywords
  (doseq [[candidate expected] (map #(vector % %) vivid.art.specs/render-phases)]
    (is (= expected
           (validate/validate-to-phase candidate)))))

(deftest unqualified-keywords-as-strings
  (doseq [[candidate expected] (map #(vector (name %) %) vivid.art.specs/render-phases)]
    (is (= expected
           (validate/validate-to-phase candidate)))))

(deftest unknown-phases
  (are [phase]
    (let [f #(validate/validate-to-phase phase)
          {:keys [step]} (special-unwind-on-signal f :vivid.art.cli/error)]
      (= 'validate-to-phase step))
    nil
    5
    ""
    " "
    "nonsense"))
