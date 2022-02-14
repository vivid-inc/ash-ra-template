; Copyright 2022 Vivid Inc.
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

(ns vivid.art.cli.dependencies-test
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [vivid.art.cli.args]
    ; Register art-cli's :dependencies -aware evaluator
    [vivid.art.cli.exec]
    [vivid.art.cli.test-lib :refer [special-unwind-on-signal]]
    [vivid.art.cli.usage :refer [cli-options]]
    [vivid.art.cli.validate :as validate]
    [vivid.art :as art])
  (:import
    (java.io PushbackReader)))

(def ^:const custom-deps
  '{compojure {:mvn/version "1.6.2"}})


;
; CLI args
;

(deftest cli-edn-file-dependencies
  (are [expected x]
    (let [args ["--dependencies" x "test-resources/empty.art"]
          {:keys [dependencies]} (vivid.art.cli.args/cli-args->batch args cli-options)]
      (= expected dependencies))
    '{hiccup {:mvn/version "1.0.5"}} "test-resources/simple-dependencies.edn"))

(deftest cli-edn-literal-dependencies
  (are [expected x]
    (let [args ["--dependencies" x "test-resources/empty.art"]
          {:keys [dependencies]} (vivid.art.cli.args/cli-args->batch args cli-options)]
      (= expected dependencies))
    '{hiccup {:mvn/version "1.0.5"}} "{hiccup {:mvn/version \"1.0.5\"}}"))

(deftest cli-malformed-dependencies
  (are [expected x]
    (= expected
       (let [f #(validate/validate-dependencies x)
             {:keys [step]} (special-unwind-on-signal f :vivid.art.cli/error)]
         step))
    'validate-dependencies ""
    'validate-dependencies " "
    'validate-dependencies "nonsense"
    'validate-dependencies "{:non"
    'validate-dependencies "sense}"
    'resolve-as-edn-file "test-resources/malformed.edn"))


;
; Internal API
;

(def ^:const vivid-art-facts (with-open [r (io/reader "../assets/vivid-art-facts.edn")]
                               (edn/read (PushbackReader. r))))

(deftest clojure-versions
  (let [versions (get vivid-art-facts "clojure-versions")]
    (doseq [version-string versions]
      (is (= (art/render "<%= (let [{:keys [major minor incremental]} *clojure-version*]
(format \"%d.%d.%d\" major minor incremental))%>"
                         {:dependencies {'org.clojure/clojure {:mvn/version version-string}}})
             version-string)))))

(deftest variants
  (are [expected x]
    (= expected
       (validate/validate-dependencies x))

    ; As a Clojure map:
    {} {}
    '{hiccup {:mvn/version "1.0.5"}} '{hiccup {:mvn/version "1.0.5"}}

    ; As symbol of qualified var:
    custom-deps #'vivid.art.cli.dependencies-test/custom-deps

    ; As a string of qualified var:
    custom-deps "vivid.art.cli.dependencies-test/custom-deps"

    ; As a string path to an EDN file:
    '{hiccup {:mvn/version "1.0.5"}} "test-resources/simple-dependencies.edn"

    ; As an EDN literal:
    '{hiccup {:mvn/version "1.0.5"}} "{hiccup {:mvn/version \"1.0.5\"}}"))

(deftest malformed-dependencies
  (are [expected x]
    (= expected
       (let [f #(validate/validate-dependencies x)
             {:keys [step]} (special-unwind-on-signal f :vivid.art.cli/error)]
         step))
    'validate-dependencies nil
    'validate-dependencies ""
    'validate-dependencies " "
    'validate-dependencies "nonsense"
    'validate-dependencies "{:non"
    'validate-dependencies "sense}"
    'validate-dependencies 'non.sense/ns
    'resolve-as-edn-file "test-resources/malformed.edn"))
