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

(ns vivid.art.clj-tool-test
  (:require
    [clojure.java.shell]
    [clojure.string]
    [clojure.test :refer :all]
    [vivid.art.clj-tool :as clj-tool]))

(deftest usage
  (let [usage (clj-tool/usage)]
    (testing "(usage) indicates how to run this tool at the CLI"
      (is (clojure.string/includes? usage "clj -m vivid.art.clj-tool")))
    (testing "(usage) mentions the overall project name"
      (is (clojure.string/includes? usage "Ash Ra")))
    (testing "(usage) mentions the ART file extension"
      (is (clojure.string/includes? usage vivid.art/art-filename-suffix)))))

(deftest clj-tool-simple
  (let [res (vivid.art.clj-tool/-main "test-resources/simple/template.txt.art"
                                      "--output-dir" "test-resources/simple")]
    (is (nil? res))
    (is (= (slurp "test-resources/simple/template.txt.expected")
           (slurp "test-resources/simple/template.txt")))))

(deftest clj-tool-full-exercise
  (let [art-res (vivid.art.clj-tool/-main
                  "--bindings" "{updated \"2021-01-01\"}"
                  "--delimiters" "{:begin-forms \"{%\" :end-forms \"%}\" :begin-eval \"{%=\" :end-eval \"%}\"}"
                  "--dependencies" "{hiccup {:mvn/version \"1.0.5\"}}"
                  "test-resources/full/templates"
                  "--output-dir" "test-resources/full/out"
                  "--to-phase" "evaluate")
        rm-res (clojure.java.shell/sh "/usr/bin/diff" "--recursive"
                                      "test-resources/full/out"
                                      "test-resources/full/expected")]
    (is (nil? art-res))
    (is (= 0 (rm-res :exit)))))

; TODO unknown option
