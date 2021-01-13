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

(ns vivid.art.leiningen-plugin-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :as t]
    [leiningen.art :as lein-art])
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

(defn all-invocation-patterns [path & art-options]
  (let [expected-dir (str path "/expected")
        target-dir (str path "/target")
        templates-dir (str path "/templates")]
    (delete-file-tree target-dir :silently)
    (let [art-args (concat [templates-dir "--output-dir" target-dir] art-options)
          art-res (apply lein-art/art nil art-args)
          diff-res (clojure.java.shell/sh "/usr/bin/diff" "--recursive"
                                          target-dir
                                          expected-dir)]
      (t/is (nil? art-res))
      (t/is (= 0 (diff-res :exit))))))

(t/deftest lein-plugin-all-options-exercise
  (all-invocation-patterns "../art/test-resources/all-options"
                           "--bindings" "{updated \"2021-01-01\"}"
                           "--delimiters" "{:begin-forms \"{%\" :end-forms \"%}\" :begin-eval \"{%=\" :end-eval \"%}\"}"
                           "--dependencies" "{hiccup {:mvn/version \"1.0.5\"}}"
                           "--to-phase" "evaluate"))

(t/deftest lein-plugin-readme-examples
  (all-invocation-patterns "../art/test-resources/readme-examples"
                           "--bindings" "{mysterious-primes [7 191]}"
                           "--delimiters" "{:begin-forms \"{%\" :end-forms \"%}\" :begin-eval \"{%=\" :end-eval \"%}\"}"))

(t/deftest lein-plugin-simple
  (all-invocation-patterns "../art/test-resources/simple"))

(t/deftest lein-plugin-utf-8
  (all-invocation-patterns "../art/test-resources/utf-8"
                           "--bindings" "../art/test-resources/utf-8/greek.edn"
                           "--delimiters" "jinja"))

; TODO Restore both calling patterns
#_(
   (ns vivid.art.leiningen-plugin-test
     (:require
       [clojure.edn]
       [clojure.java.io :as io]
       [clojure.string]
       [clojure.test :refer :all]
       [leiningen.art :as lein-art]
       [leiningen.core.project :as lein-prj]
       [vivid.art.cli.exec]
       [vivid.art.cli.resolve]
       [vivid.art.delimiters])
     (:import
       (java.io File)
       (java.net URL)))

   (def test-project (lein-prj/read (^URL .getFile (io/resource "test-project.clj"))))

   (defn project-stanza->cli-args
     [{:keys [templates bindings delimiters output-dir]}]
     (let [cc (fn [xs x] (concat xs (if (coll? x) x [x])))]
       (cond-> []
               templates (cc templates)
               bindings (cc bindings)
               delimiters (cc ["--delimiters" delimiters])
               output-dir (cc ["--output-dir" output-dir]))))

   (defn call-art-via-cli-args
     [project-stanza delimiters output-dir]
     (let [s (merge project-stanza
                    (when delimiters
                      {:delimiters delimiters})
                    {:output-dir output-dir})
           args (concat [nil] (project-stanza->cli-args s))]
       (apply lein-art/art args)))

   (defn call-art-via-project-stanza
     [project-stanza delimiters output-dir]
     (let [s (merge project-stanza
                    (when delimiters
                      {:delimiters (vivid.art.cli.resolve/resolve-delimiters delimiters)})
                    {:output-dir output-dir})
           prj {:art s}]
       (lein-art/art prj)))

   (defn invocation-pattern
     [data call-fn call-name]
     (testing call-name
              (let [{:keys [dir expected actual stanza delimiters]} data
                    target-dir (str "target/art-rendered/" dir "-" call-name)]
                (delete-file-tree target-dir :silently)
                (call-fn stanza delimiters target-dir)
                (is (= (slurp expected)
                       (slurp (str target-dir "/" actual)))))))

   (def calls
     [[call-art-via-cli-args "cli-args"]
      [call-art-via-project-stanza "project-stanza"]])

   (defn all-invocation-patterns
     [data]
     (doseq [[call-fn call-name] calls]
       (invocation-pattern data call-fn call-name)))
   )
