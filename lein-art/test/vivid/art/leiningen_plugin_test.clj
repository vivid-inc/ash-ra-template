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
  [project-stanza]
  (let [as-coll (fn [x] (if (coll? x) x [x]))
        templates (as-coll (:templates project-stanza))
        opts (reduce-kv (fn [acc k v] (concat acc [(str "--" (name k)) v]))
                        []
                        (dissoc project-stanza :templates))
        args (concat templates opts)]
    (apply lein-art/art nil "render" args)))

(defn call-art-via-project-stanza
  [project-stanza]
  (let [project {:art project-stanza}]
    (lein-art/art project "render")))

(def calls
  [[call-art-via-cli-args "cli-args"]
   [call-art-via-project-stanza "project-stanza"]])

(defn invocation-pattern
  [call-fn call-name path art-options]
  (t/testing call-name
    (let [expected-dir (str path "/expected")
          target-dir (str path "/target")
          templates-dir (str path "/templates")]
      (delete-file-tree target-dir :silently)
      (let [project-stanza (merge art-options
                                  {:templates templates-dir
                                   :output-dir target-dir})
            art-res (call-fn project-stanza)
            diff-res (clojure.java.shell/sh "/usr/bin/diff" "--recursive"
                                            target-dir
                                            expected-dir)
            test-failure-message (pr-str {:project-stanza project-stanza
                                          :art-res        art-res
                                          :diff-res       diff-res})]
        (t/is (nil? art-res) test-failure-message)
        (t/is (= 0 (diff-res :exit)) test-failure-message)))))

(defn all-invocation-patterns
  [path art-options]
  (doseq [[call-fn call-name] calls]
    (invocation-pattern call-fn call-name path art-options)))

(t/deftest lein-plugin-all-options-exercise
  (all-invocation-patterns "../examples/all-options"
                           {:bindings '{updated "2021-01-01"}
                            :delimiters {:begin-forms "{%" :end-forms "%}" :begin-eval "{%=" :end-eval "%}"}
                            :dependencies '[[hiccup/hiccup "1.0.5"]]
                            :to-phase :evaluate}))

(t/deftest lein-plugin-readme-examples
  (all-invocation-patterns "../examples/readme-examples"
                           {:bindings '{mysterious-primes [7 191]}
                            :delimiters {:begin-forms "{%" :end-forms "%}" :begin-eval "{%=" :end-eval "%}"}}))

(t/deftest lein-plugin-simple
  (all-invocation-patterns "../examples/simple"
                           {}))

(t/deftest lein-plugin-utf-8
  (all-invocation-patterns "../examples/utf-8"
                           {:bindings "../examples/utf-8/greek.edn"
                            :delimiters 'jinja}))

(t/deftest lein-plugin-art-example-custom-options
  (let [res (clojure.java.shell/sh "./test.sh" "lein" "do" "clean," "install," "art" "render"
                                   :dir "../examples/custom-options")]
    (t/is (= 0 (res :exit))
          (pr-str res))))

(t/deftest lein-plugin-example-multi-batch
  (let [res (clojure.java.shell/sh "./test.sh" "lein" "art" "render"
                                   :dir "../examples/multi-batch")]
    (t/is (= 0 (res :exit))
          (pr-str res))))

(t/deftest lein-plugin-example-watch
  (all-invocation-patterns "../examples/watch"
                           {}))
