; Copyright 2021 Vivid Inc.
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
    [clojure.java.io :as io]
    [clojure.java.shell]
    [clojure.string]
    [clojure.test :as t]
    [vivid.art.cli]
    [vivid.art.clj-tool :as clj-tool])
  (:import
    (java.io File)))

(defn delete-file-tree
  [path & [silently]]
  ((fn del [^File file]
     (when (.isDirectory file)
       (doseq [child (.listFiles file)]
         (del child)))
     (io/delete-file file silently))
   (io/file path)))

(defn invocation-pattern
  [path & command]
  (let [expected-dir (str path "/expected")
        target-dir (str path "/target")]
    (delete-file-tree target-dir :silently)
    (let [clj-res (apply clojure.java.shell/sh (concat command [:dir path]))
          diff-res (clojure.java.shell/sh "/usr/bin/diff" "--recursive"
                                          target-dir
                                          expected-dir)
          test-failure-message (pr-str {:clj-res  clj-res
                                        :diff-res diff-res})]
      (t/is (= 0 (clj-res :exit)) test-failure-message)
      (t/is (= 0 (diff-res :exit)) test-failure-message))))

(t/deftest usage
  (let [usage (clj-tool/usage)]
    (t/testing "(usage) indicates how to run this tool at the CLI"
      (t/is (clojure.string/includes? usage "clj -m vivid.art.clj-tool")))
    (t/testing "(usage) mentions the overall project name"
      (t/is (clojure.string/includes? usage "Ash Ra")))
    (t/testing "(usage) mentions the ART file extension"
      (t/is (clojure.string/includes? usage vivid.art.cli/art-filename-suffix)))))

(t/deftest clj-tool-example-all-options
  (invocation-pattern "../examples/all-options"
                      "clj" "-M:art"))

; TODO Test example-custom-options

(t/deftest clj-tool-example-multi-batch
  (let [target-a "expected-src-resources"
        target-b "expected-target-generated-sources-java"]
    (doseq [dir [target-a target-b]]
      (delete-file-tree dir :silently)))
  (let [res (clojure.java.shell/sh "./test.sh" "clj-art"
                                   :dir "../examples/multi-batch")]
    (t/is (= 0 (res :exit))
          (pr-str {:res res}))))

(t/deftest clj-tool-example-override-clojure-version
  (invocation-pattern "../examples/readme-examples"
                      "clj" "-M:art"))

(t/deftest clj-tool-example-readme-examples
  (invocation-pattern "../examples/readme-examples"
                      "clj" "-M:art"))

(t/deftest clj-tool-example-simple
  (invocation-pattern "../examples/simple"
                      "clj" "-M:art"))

(t/deftest clj-tool-example-utf-8
  (invocation-pattern "../examples/utf-8"
                      "clj" "-M:art"))
