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

(ns vivid.art.cli.dependencies-test
  (:require
    [clojure.test :refer [are deftest]]
    [farolero.core :as farolero]
    [vivid.art.cli.args]
    [vivid.art.cli.usage :refer [cli-options]]
    [vivid.art.cli.validate :as validate]))

(def ^:const custom-deps
  '[[compojure/compojure "1.6.2"]])


;
; CLI args
;

(deftest cli-edn-file-dependencies
  (are [expected x]
    (let [args ["--dependencies" x "test-resources/empty.art"]
          {:keys [dependencies]} (vivid.art.cli.args/cli-args->batch args cli-options)]
      (= expected dependencies))
    '[[hiccup/hiccup "1.0.5"]] "test-resources/simple-dependencies.edn"))

(deftest cli-edn-literal-dependencies
  (are [expected x]
    (let [args ["--dependencies" x "test-resources/empty.art"]
          {:keys [dependencies]} (vivid.art.cli.args/cli-args->batch args cli-options)]
      (= expected dependencies))
       '[[org.suskalo/farolero "1.4.3"]] "[[org.suskalo/farolero \"1.4.3\"]]"))

(deftest cli-malformed-dependencies
  (are [expected x]
    (= expected
       (farolero/handler-case (validate/validate-dependencies x)
                              (:vivid.art.cli/error [_ {:keys [step]}] step)))
    'validate-dependencies ""
    'validate-dependencies " "
    'validate-dependencies "nonsense"
    'validate-dependencies "{:non"
    'validate-dependencies "sense}"
    'resolve-as-edn-file "test-resources/malformed.edn"))


;
; Internal API
;

(deftest variants
  (are [expected x]
    (= expected
       (validate/validate-dependencies x))

    ; As a Leiningen dependency list:
    [] []
    '[[ring/ring-core "1.9.5"]] '[[ring/ring-core "1.9.5"]]

    ; As symbol of qualified var:
    custom-deps #'vivid.art.cli.dependencies-test/custom-deps

    ; As a string of qualified var:
    custom-deps "vivid.art.cli.dependencies-test/custom-deps"

    ; As a string path to an EDN file:
    '[[hiccup/hiccup "1.0.5"]] "test-resources/simple-dependencies.edn"

    ; As an EDN literal:
    '[[clj-http/clj-http "3.12.3"]] "[[clj-http/clj-http \"3.12.3\"]]"))

(deftest malformed-dependencies
  (are [expected x]
    (= expected
       (farolero/handler-case (validate/validate-dependencies x)
                              (:vivid.art.cli/error [_ {:keys [step]}] step)))
    'validate-dependencies nil
    'validate-dependencies ""
    'validate-dependencies " "
    'validate-dependencies "nonsense"
    'validate-dependencies "{:non"
    'validate-dependencies "sense}"
    'validate-dependencies 'non.sense/ns
    'resolve-as-edn-file "test-resources/malformed.edn"))
