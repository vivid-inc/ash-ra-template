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

(ns vivid.art.cli.to-phase-test
  (:require
    [clojure.string]
    [clojure.test :refer [are deftest is]]
    [farolero.core :as farolero]
    [vivid.art.cli.args]
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
          {:keys [step message]} (farolero/handler-case (vivid.art.cli.args/cli-args->batch args vivid.art.cli.usage/cli-options)
                                                        (:vivid.art.cli/error [_ details] details))]
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
    (= 'validate-to-phase
       (farolero/handler-case (validate/validate-to-phase phase)
                              (:vivid.art.cli/error [_ {:keys [step]}] step)))
    nil
    5
    ""
    " "
    "nonsense"))
