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

(ns vivid.art.boot-task-test
  (:require
    [boot.core :as boot]
    [boot.test]
    [clojure.java.io :as io]
    [clojure.string]
    [clojure.test :as t]
    [vivid.boot-art])
  (:import
    (java.io File)))

(defn clj-filename?
  [f]
  (clojure.string/ends-with? f ".clj"))

(defn fs-paths [fileset]
  (->> fileset
       (boot/ls)
       (map boot/tmp-path)
       (remove clj-filename?)
       sort))

(defn fs-contents [fileset]
  (->> fileset
       (boot/ls)
       (remove (comp clj-filename? boot/tmp-path))
       (sort-by boot/tmp-path)
       (map boot/tmp-file)
       (map slurp)))

(boot/deftask expect
              [_ paths VAL [file] "paths to files expected paths"]
              (boot/with-pass-thru fileset
                                   (let [expected-contents (map slurp paths)
                                         output-filenames (map #(.getName %) paths)]
                                     (t/testing "Rendered template file output path"
                                       (t/is (= output-filenames (fs-paths fileset))))
                                     (t/testing "Rendered template file output content"
                                       (t/is (= expected-contents (fs-contents fileset)))))))

(boot/deftask populate
              "Populates the Boot working set of files with templates"
              [_ paths VAL [file] "template file paths"]
              (boot/with-pre-wrap fileset
                                  (let [tmp-dir (boot/tmp-dir!)]
                                    (doseq [path paths]
                                      (let [content (slurp path)]
                                        (as-> path p
                                              (.getName p)
                                              (io/file tmp-dir p)
                                              (doto p io/make-parents)
                                              (spit p content))))
                                    (-> fileset
                                        (boot/add-resource tmp-dir)
                                        boot/commit!))))

(boot/deftask shell-cmd
              [_ cmd VAL [str] "Run this command and args"
               _ dir VAL str "Run in this directory"]
              (boot/with-pass-thru fileset
                                   (let [c (concat cmd [:dir dir])
                                         res (apply clojure.java.shell/sh c)]
                                     (t/is (= 0 (res :exit))))))

(defn files-under-dir [dir]
  (->> (io/file dir)
       (file-seq)
       (filter #(.isFile %))
       (sort)))

(defn all-invocation-patterns
  ([^String dir] (all-invocation-patterns dir nil))
  ([^String dir & art-options]
   (let [templates (->> (files-under-dir (File. dir "templates"))
                        (filter vivid.art.cli.files/art-template-file?))
         expected-output (files-under-dir (File. dir "expected"))]
     (comp (populate :paths templates)
           (apply vivid.boot-art/art art-options)
           (expect :paths expected-output)))))

(boot.test/deftesttask
  boot-task-all-options []
  (all-invocation-patterns "../examples/all-options"
                           :bindings     '{updated "2021-01-01"}
                           :delimiters   '{:begin-forms "{%" :end-forms "%}" :begin-eval "{%=" :end-eval "%}"}
                           :dependencies '{hiccup {:mvn/version "1.0.5"}}
                           :to-phase     :evaluate))

(boot.test/deftesttask
  boot-task-readme-examples []
  (all-invocation-patterns "../examples/readme-examples"
                           :bindings '{mysterious-primes [7 191]}
                           :delimiters '{:begin-forms "{%" :end-forms "%}" :begin-eval "{%=" :end-eval "%}"}))

(boot.test/deftesttask
  boot-task-simple []
  (all-invocation-patterns "../examples/simple"))

(boot.test/deftesttask
  boot-task-utf-8 []
  (all-invocation-patterns "../examples/utf-8"
                           :bindings (read-string (slurp "../examples/utf-8/greek.edn"))
                           :delimiters vivid.art.delimiters/jinja))



(boot.test/deftesttask
  boot-task-example-boot-templates-output-dir []
  (comp (shell-cmd :cmd ["./test.sh"]
                   :dir "../examples/boot-templates-output-dir")))

(boot.test/deftesttask
  boot-task-example-custom-options []
  (comp (shell-cmd :cmd ["./test.sh" "boot" "rndr"]
                   :dir "../examples/custom-options")))

(boot.test/deftesttask
  boot-task-example-multi-batch []
  (comp (shell-cmd :cmd ["./test.sh" "boot" "rndr"]
                   :dir "../examples/multi-batch")))

(boot.test/deftesttask
  boot-task-example-override-clojure-version []
  (all-invocation-patterns "../examples/override-clojure-version"
                           :dependencies '{org.clojure/clojure {:mvn/version "1.10.1"}}))

(boot.test/deftesttask
  boot-task-example-watch []
  (all-invocation-patterns "../examples/watch"))
