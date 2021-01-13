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

(ns vivid.art.cli.cli-args-test
  (:require
    [clojure.string]
    [clojure.test :refer :all]
    [vivid.art.specs]
    [vivid.art.cli.args]
    [vivid.art.cli.test-lib :refer [special-unwind-on-signal]]
    [vivid.art.cli.usage :refer [cli-options]])
  (:import
    (java.io File)))

(deftest command-help
  (are [args]
    (true?
       (let [f #(vivid.art.cli.args/cli-args->batch args cli-options)
             data (special-unwind-on-signal f :vivid.art.cli/error)]
         (:show-usage data)))
    []
    ["-h"]
    ["--help"]))

(deftest templates-args
  (are [args expected]
    (= expected
       (vivid.art.cli.args/cli-args->batch args cli-options))

    ["test-resources/empty.art"]
    {:output-dir (File. ^String vivid.art.cli.usage/default-output-dir)
     :templates  (list {:src-path (File. "test-resources/empty.art")
                        :dest-rel-path (File. "empty")})}))

(deftest bad-template-args
  (are [filename]
    (= 'validate-templates
       (let [args [filename]
             f #(vivid.art.cli.args/cli-args->batch args cli-options)
             data (special-unwind-on-signal f :vivid.art.cli/error)]
         (:step data)))
    ""
    " "
    "bogus-98cbb569-0a7b-4534-bffe-418944f97686.art"))

(deftest unknown-args
  (are [args]
    (let [f #(vivid.art.cli.args/cli-args->batch args cli-options)
          {:keys [step message]} (special-unwind-on-signal f :vivid.art.cli/error)]
      (and (= 'parse-cli-args step)
           (clojure.string/includes? message (first args))))
    ["--nonsense"]))
