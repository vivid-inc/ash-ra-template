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

(defn all-invocation-patterns
  [path & art-options]
  (let [expected-dir (str path "/expected")
        target-dir (str path "/target")
        templates-dir (str path "/templates")]
    (delete-file-tree target-dir :silently)
    (let [art-args (concat [templates-dir "--output-dir" target-dir] art-options)
          art-res (apply vivid.art.clj-tool/-main art-args)
          diff-res (clojure.java.shell/sh "/usr/bin/diff" "--recursive"
                                          target-dir
                                          expected-dir)]
      (t/is (nil? art-res))
      (t/is (= 0 (diff-res :exit))))))

(t/deftest usage
  (let [usage (clj-tool/usage)]
    (t/testing "(usage) indicates how to run this tool at the CLI"
      (t/is (clojure.string/includes? usage "clj -m vivid.art.clj-tool")))
    (t/testing "(usage) mentions the overall project name"
      (t/is (clojure.string/includes? usage "Ash Ra")))
    (t/testing "(usage) mentions the ART file extension"
      (t/is (clojure.string/includes? usage vivid.art/art-filename-suffix)))))

(t/deftest clj-tool-all-options-exercise
  (all-invocation-patterns "../examples/all-options"
                           "--bindings" "{updated \"2021-01-01\"}"
                           "--delimiters" "{:begin-forms \"{%\" :end-forms \"%}\" :begin-eval \"{%=\" :end-eval \"%}\"}"
                           "--dependencies" "{hiccup {:mvn/version \"1.0.5\"}}"
                           "--to-phase" "evaluate"))

(t/deftest clj-tool-readme-examples
  (all-invocation-patterns "../examples/readme-examples"
                           "--bindings" "{mysterious-primes [7 191]}"
                           "--delimiters" "{:begin-forms \"{%\" :end-forms \"%}\" :begin-eval \"{%=\" :end-eval \"%}\"}"))

(t/deftest clj-tool-simple
  (all-invocation-patterns "../examples/simple"))

(t/deftest clj-tool-utf-8
  (all-invocation-patterns "../examples/utf-8"
                           "--bindings" "../examples/utf-8/greek.edn"
                           "--delimiters" "jinja"))
