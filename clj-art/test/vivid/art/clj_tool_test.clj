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

; TODO Equalize with vivid.art.leiningen-plugin-test

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

(def common-project-stanza-invocation-pattern
  {:command                   ["clj" "-M:art" "render"]
   :target-and-expected-dirs  [["target" "expected"]]})

(defn invocation-pattern
  [p]
  (let [{:keys [dir command target-and-expected-dirs]}
        (merge common-project-stanza-invocation-pattern
               p)]
    (doseq [target-dir (map first target-and-expected-dirs)]
      (delete-file-tree (str dir "/" target-dir) :silently))
    (let [exec-result (if (fn? command) (command p)
                          (apply clojure.java.shell/sh (concat command [:dir dir])))]
      (doseq [[target-dir' expected-dir'] target-and-expected-dirs]
        (let [target-dir            (str dir "/" target-dir')
              expected-dir          (str dir "/" expected-dir')
              diff-result           (clojure.java.shell/sh "/usr/bin/diff" "--recursive"
                                                           target-dir expected-dir)
              test-failure-message  (pr-str {:exec-res exec-result
                                             :diff-res diff-result})]
          (t/is (= 0 (exec-result :exit)) test-failure-message)
          (t/is (= 0 (diff-result :exit)) test-failure-message))))))

(t/deftest usage
  (let [usage (clj-tool/usage)]
    (t/testing "(usage) indicates how to run this tool at the CLI"
      (t/is (clojure.string/includes? usage "clj -m vivid.art.clj-tool")))
    (t/testing "(usage) mentions the overall project name"
      (t/is (clojure.string/includes? usage "Ash Ra")))
    (t/testing "(usage) mentions the ART file extension"
      (t/is (clojure.string/includes? usage vivid.art.cli/art-filename-suffix)))))

(t/deftest clj-tool-example-all-options
  (invocation-pattern
   {:dir "../examples/all-options"}))

(t/deftest clj-tool-example-custom-options
  (invocation-pattern
   {:command (fn [_]
               (let [exec-result-0 (apply clojure.java.shell/sh (concat ["lein" "do" "clean," "install"] [:dir "../examples/custom-options"]))]
                 (if (not= 0 (exec-result-0 :exit))
                   exec-result-0
                   (apply clojure.java.shell/sh (concat ["clj" "-M:art" "render"] [:dir "../examples/custom-options"])))))
    :dir                      "../examples/custom-options"
    :target-and-expected-dirs [["out/cdn" "expected"]]}))

(t/deftest clj-tool-example-multi-batch
  (invocation-pattern
   {:dir                      "../examples/multi-batch"
    :target-and-expected-dirs [["src/resources"                  "expected-src-resources"]
                               ["target/generated-sources/java"  "expected-target-generated-sources-java"]
                               ["www"                            "expected-www"]]}))

(t/deftest clj-tool-example-readme-examples
  (invocation-pattern
   {:dir "../examples/readme-examples"}))

(t/deftest clj-tool-example-simple
  (invocation-pattern
   {:dir "../examples/simple"}))

(t/deftest clj-tool-example-utf-8
  (invocation-pattern
   {:dir "../examples/utf-8"}))

; TODO clj-tool-example-watch
