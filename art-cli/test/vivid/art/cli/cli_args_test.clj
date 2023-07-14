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

(ns vivid.art.cli.cli-args-test
  (:require
   [clojure.string]
   [clojure.test :refer [are deftest]]
   [farolero.core :as farolero]
   [vivid.art.specs]
   [vivid.art.cli.args]
   [vivid.art.cli.usage :refer [cli-options]])
  (:import
   (java.io File)))

(deftest command-help
  (are [args]
       (true?
        (farolero/handler-case (vivid.art.cli.args/cli-args->batch args cli-options)
                               (:vivid.art.cli/error [_ {:keys [show-usage]}] show-usage)))
    []
    ["-h"]
    ["--help"]))

(deftest templates-args
  (are [args expected]
       (= expected
          (vivid.art.cli.args/cli-args->batch args cli-options))

    ["test-resources/empty.art"]
    {:output-dir (.getAbsoluteFile (File. ^String vivid.art.cli.usage/default-output-dir))
     :templates  (list (.getAbsoluteFile (File. "test-resources/empty.art")))}))

(deftest bad-template-args
  (are [filename]
       (= 'validate-templates
          (let [args [filename]]
            (farolero/handler-case
           ; Coax farolero to signal the expected ::error by forcing evaluation
             (doall (:templates (vivid.art.cli.args/cli-args->batch args cli-options)))
             (:vivid.art.cli/error [_ {:keys [step]}] step))))
    ""
    " "
    "bogus-98cbb569-0a7b-4534-bffe-418944f97686.art"))

(deftest unknown-args
  (are [args]
       (let [{:keys [step message]} (farolero/handler-case (vivid.art.cli.args/cli-args->batch args cli-options)
                                                           (:vivid.art.cli/error [_ details] details))]
         (and (= 'parse-cli-args step)
              (clojure.string/includes? message (first args))))
    ["--nonsense"]))
