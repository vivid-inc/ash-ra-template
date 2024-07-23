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

(defn call-art-via-cli-args
  [dir batch]
  (let [as-coll   (fn [x] (if (coll? x) x [x]))
        templates (as-coll (:templates batch))
        bindings  (interleave (repeat "--bindings") (:bindings batch))
        opts      (reduce-kv (fn [acc k v] (concat acc [(str "--" (name k)) v]))
                             []
                             (dissoc batch :templates :bindings))
        args      (->> (concat templates bindings opts)              ; TODO Conversion heuristic needs further refinement
                       (map #(if (string? %)
                               %
                               (pr-str %))))
        cmd       (concat ["lein" "art" "render"] args [:dir dir]) ; TODO Remove CLASSPATH from current env.
        _ (println "cmd" (pr-str cmd))
        exec-res  (apply clojure.java.shell/sh cmd)]
    (println "exec-res" (pr-str exec-res))
    exec-res))

(defn call-art-via-project-stanza
  [_dir batches]
  (let [project {:art batches}
        res     (lein-art/art project "render")]            ; TODO working directory
    {:exit (if (nil? res) 0 -1)}))

(def common-project-stanza-invocation-pattern
  {:target-and-expected-dirs  [["target" "expected"]]})

(def calls
  [#_[call-art-via-cli-args       "cli-args"]
   [call-art-via-project-stanza "project-stanza"]])

(defn invocation-pattern*
  [call-fn call-name p]
  (t/testing call-name
    (let [{:keys [dir batches target-and-expected-dirs]}
          (merge common-project-stanza-invocation-pattern
                 p)]

      ; Clean output directories
      (doseq [target-dir (map first target-and-expected-dirs)]
        (delete-file-tree (str dir "/" target-dir) :silently))

      ; Render the batches
      (doseq [batch batches]
        (let [exec-result (call-fn dir batch)
              test-failure-message  (pr-str {:call-name     call-name
                                             :dir           dir
                                             :batch         batch
                                             :exec-result   exec-result})]
          (t/is (zero? (exec-result :exit)) test-failure-message)))

      ; Test for expected output
      (doseq [[target-dir' expected-dir'] target-and-expected-dirs]
        (let [target-dir            (str dir "/" target-dir')
              expected-dir          (str dir "/" expected-dir')
              diff-result           (clojure.java.shell/sh "/usr/bin/diff" "--recursive"
                                                           target-dir expected-dir)
              test-failure-message  (pr-str {:call-name     call-name
                                             :dir           dir
                                             :batches       batches
                                             :target-dir    target-dir
                                             :expected-dir  expected-dir
                                             :diff-result   diff-result})]
          (t/is (zero? (diff-result :exit)) test-failure-message))))))

(defn invocation-pattern
  [p]
  (doseq [[call-fn call-name] calls]
    (invocation-pattern* call-fn call-name p)))

(t/deftest usage
  (let [usage (lein-art/usage)]
    (t/testing "(usage) indicates how to run this tool at the CLI"
      (t/is (clojure.string/includes? usage "lein art")))
    (t/testing "(usage) mentions the overall project name"
      (t/is (clojure.string/includes? usage "Ash Ra")))
    (t/testing "(usage) mentions the ART file extension"
      (t/is (clojure.string/includes? usage vivid.art.cli/art-filename-suffix)))))

(t/deftest lein-plugin-example-all-options
  (invocation-pattern
   {:dir      "../examples/all-options"
    :batches  [{:bindings     '{updated "2021-01-01"}
                :dependencies '[[hiccup/hiccup "1.0.5" :exclusions [org.clojure/clojure]]]
                :delimiters   {:begin-forms "{%" :end-forms "%}" :begin-eval "{%=" :end-eval "%}"}
                :output-dir   "target"
                :templates    "templates"
                :to-phase     :evaluate                     ; TODO Accept stringified keywords in (validate)
                }]}))

(t/deftest lein-plugin-example-custom-options
  (invocation-pattern
   {:dir                      "../examples/custom-options"
    :batches                  [{:templates    "content"

                                :bindings     ['{manufacturer     "Acme Corporation"    ; Map literal
                                                manufacture-year "2022"}

                                               ; TODO #'com.acme.data/product-data                 ; Var, value is a map
                                               ; Its value is copy & pasted here:
                                               '{products [{:name               "Bag of bird seed"
                                                           :weight-kgs         1.0
                                                           :minimum-order-qty  50
                                                           :unit-price-dollars 0.39M}
                                                          {:name               "Ironing board on rollerskates"
                                                           :weight-kgs         2.0
                                                           :minimum-order-qty  10
                                                           :unit-price-dollars 17.95M}]}

                                               "{current-year 2021}"                   ; EDN as a string
                                               "data/sales-offices.edn"                ; EDN file; top-level form is a map
                                               "data/partner-list.json"]               ; JSON file; file content is made available under the symbol 'partner-list

                                :delimiters   "jinja"                                  ; Resolves to #'vivid.art.delimiters/jinja

                                :dependencies '[[hiccup/hiccup "1.0.5" :exclusions [org.clojure/clojure]]]

                                :output-dir   "out/cdn"}]
    :target-and-expected-dirs [["out/cdn" "expected"]]}))

(t/deftest lein-plugin-example-multi-batch
  (invocation-pattern
   {:dir                      "../examples/multi-batch"
    :batches                  [{:templates    "src/templates/css"
                                :dependencies '[[garden/garden "1.3.10"]]
                                :output-dir   "src/resources"}
                               {:templates    ["src/templates/java"]
                                :bindings     '{version "1.2.3"}
                                :output-dir   "target/generated-sources/java"}
                               {:templates    "src/templates/html"
                                :dependencies '[[hiccup/hiccup "1.0.5"]]
                                :output-dir   "www"}]
    :target-and-expected-dirs [["src/resources"                  "expected-src-resources"]
                               ["target/generated-sources/java"  "expected-target-generated-sources-java"]
                               ["www"                            "expected-www"]]}))

(t/deftest lein-plugin-example-readme-examples
  (invocation-pattern
   {:dir     "../examples/readme-examples"
    :batches [{:bindings   '{mysterious-primes [7 191]}
               :templates  "templates/oracle.art"
               :output-dir "target"}]}))

(t/deftest lein-plugin-example-simple
  (invocation-pattern
   {:dir      "../examples/simple"
    :batches  [{:templates    "templates"
                :output-dir   "target"}]}))

(t/deftest lein-plugin-example-utf-8
  (invocation-pattern
   {:dir      "../examples/utf-8"
    :batches  [{:templates    "templates"
                :output-dir   "target"
                :bindings     "greek.edn"
                :delimiters   'jinja}]}))

(t/deftest lein-plugin-example-watch
  ; Actual watching behavior is not tested here.
  ; TODO restore source file, wrap the shell sub-process, sleep twice the watch interval, test 1st expected output, modify source file, then after sleeping twice the watch interval, test 2nd expected output.
  (invocation-pattern
   {:dir      "../examples/watch"
    :batches [{:templates    "resources"
               :output-dir   "target"}]}))
