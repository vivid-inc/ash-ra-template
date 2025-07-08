; Copyright 2024 Vivid Inc. and/or its affiliates
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

(ns vivid.art.cli.watch-timeout-ms-test
  (:require
   [clojure.test :refer [are deftest]]
   [farolero.core :as farolero]
   [vivid.art.cli.args]
   [vivid.art.cli.debounce]
   [vivid.art.cli.log]
   [vivid.art.cli.usage :refer [cli-options]]
   [vivid.art.cli.validate :as validate]))

(assert (= 10 vivid.art.cli.debounce/core-async-timeout-resolution))

;
; CLI args
;

(deftest cli-watch-timeout-ms
  (with-redefs [vivid.art.cli.log/*warn-fn* (fn [& _])]
    (are [expected input]
         (let [args                        ["--watch-timeout-ms" input "test-resources/empty.art"]
               {:keys [watch-timeout-ms]}  (vivid.art.cli.args/cli-args->batch args cli-options)]
           (= expected watch-timeout-ms))
      vivid.art.cli.debounce/core-async-timeout-resolution "-239847"
      vivid.art.cli.debounce/core-async-timeout-resolution "0"
      vivid.art.cli.debounce/core-async-timeout-resolution "1"
      vivid.art.cli.debounce/core-async-timeout-resolution "9"
      10 "10"
      11 "11"
      333 "333"
      1234567890 "1234567890")))

(deftest cli-invalid-watch-timeout-ms
  (are [input]
       (= 'parse-cli-args
          (let [args ["--watch-timeout-ms" input "test-resources/empty.art"]]
            (farolero/handler-case (vivid.art.cli.args/cli-args->batch args cli-options)
                                   (:vivid.art.cli/error [_ {:keys [step]}] step))))
    ""
    " "
    "a"
    "a100"
    "1j00"
    "10q0"
    "100z"
    ";"))

;
; Validators
;

(deftest validator-watch-timeout-ms
  (with-redefs [vivid.art.cli.log/*warn-fn* (fn [& _])]
    (are [expected input]
         (= expected
            (validate/validate-watch-timeout-ms input))
      vivid.art.cli.debounce/core-async-timeout-resolution "-239847"
      vivid.art.cli.debounce/core-async-timeout-resolution "0"
      vivid.art.cli.debounce/core-async-timeout-resolution "1"
      vivid.art.cli.debounce/core-async-timeout-resolution "9"
      10 "10"
      11 "11"
      333 "333"
      1234567890 "1234567890")))

(deftest validator-invalid-watch-timeout-ms
  (are [input]
       (= 'validate-watch-timeout-ms
          (farolero/handler-case (validate/validate-watch-timeout-ms input)
                                 (:vivid.art.cli/error [_ {:keys [step]}] step)))
    ""
    " "
    "a"
    "a100"
    "1j00"
    "10q0"
    "100z"
    ";"))
