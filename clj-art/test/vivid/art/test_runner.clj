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

(ns vivid.art.test-runner
  (:require
   [eftest.report]
   [eftest.report.junit]
   [eftest.runner]))

(defn run-tests
  [junit?]
  (eftest.runner/run-tests
   (eftest.runner/find-tests "test")
   (when junit?
     {:report (eftest.report/report-to-file eftest.report.junit/report
                                            "target/junit.xml")})))

(defn -main
  "Entry point for the Eftest test runner."
  ; Classpath is already set by Clojure deps tool.
  [& args]
  (try
    (let [junit? (= (first args) "--junit")                 ; TODO Find how to pass this CLI arg
          {:keys [error fail]} (run-tests junit?)]
      (when (pos? (+ error fail))
        (System/exit 1)))
    (finally
      (shutdown-agents))))
