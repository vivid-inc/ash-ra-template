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

(ns vivid.art.cli.bindings-test
  (:require
    [clojure.test :refer :all]
    [vivid.art.cli.args]
    [vivid.art.cli.test-lib :refer [special-unwind-on-signal]]
    [vivid.art.cli.usage :refer [cli-options]]
    [vivid.art.cli.validate :as validate]))

(def ^:const custom-bindings
  {:b 2})


;
; CLI args
;

(deftest cli-edn-file-bindings
  (are [expected x]
    (let [args ["--bindings" x "test-resources/empty.art"]
          {:keys [bindings]} (vivid.art.cli.args/cli-args->batch args cli-options)]
      (= expected bindings))
    {:a 1} "test-resources/simple-bindings.edn"))

(deftest cli-edn-literal-bindings
  (are [expected x]
    (let [args ["--bindings" x "test-resources/empty.art"]
          {:keys [bindings]} (vivid.art.cli.args/cli-args->batch args cli-options)]
      (= expected bindings))
    {:a 1 :b 2} "{:a 1 :b 2}"))

(deftest cli-malformed-bindings
  (are [expected x]
    (= expected
       (let [f #(validate/validate-bindings x)
             {:keys [step]} (special-unwind-on-signal f :vivid.art.cli/error)]
         step))
    'validate-bindings ""
    'validate-bindings " "
    'validate-bindings "nonsense"
    'validate-bindings "{:non"
    'validate-bindings "sense}"
    'resolve-as-edn-file "test-resources/malformed.edn"))


;
; Internal API
;

(deftest variants
  (are [expected x]
    (= expected
       (validate/validate-bindings x))

    ; As Clojure map:
    ; Empty
    {} {}
    ; Single binding definition
    {:a 1 :b 2} {:a 1 :b 2}
    ; Collection of a single binding definition
    {:a 1} [{:a 1}]
    ; Collection of several, disjoint binding definitions
    {:a 1 :b 2} [{:a 1} {:b 2}]
    {:a 1 :b 2} [{:b 2} {:a 1}]
    ; Collection of several, conflicting binding definitions. Older map keys'
    ; values are clobbered in order of appearance in the collection.
    {:a 2 :b 9} [{:a 1 :b 9} {:a 2}]
    {:a 1 :b 9} [{:a 2} {:a 1 :b 9}]

    ; As symbol of qualified var:
    custom-bindings #'vivid.art.cli.bindings-test/custom-bindings

    ; As string of qualified var:
    custom-bindings "vivid.art.cli.bindings-test/custom-bindings"

    ; As a string path to an EDN file:
    {:a 1} "test-resources/simple-bindings.edn"

    ; As EDN literal:
    custom-bindings (pr-str custom-bindings)))

(deftest mixed-bindings
  (are [expected x]
    (= expected
       (validate/validate-bindings x))
    {:a 1 :b 2 :c 3 :d 4} ["test-resources/simple-bindings.edn"
                           'vivid.art.cli.bindings-test/custom-bindings
                           {:c 3}
                           "{:d 4}"]))

(deftest malformed-bindings
  (are [expected x]
    (= expected
       (let [f #(validate/validate-bindings x)
             {:keys [step]} (special-unwind-on-signal f :vivid.art.cli/error)]
         step))
    'validate-bindings nil
    'validate-bindings ""
    'validate-bindings " "
    'validate-bindings "nonsense"
    'validate-bindings "{:non"
    'validate-bindings "sense}"
    'validate-bindings 'non.sense/ns
    'resolve-as-edn-file "test-resources/malformed.edn"))

(deftest mixed-bindings-one-bad
  (is (= 'validate-bindings
         (let [x ["test-resources/simple-bindings.edn"
                  'vivid.art.cli.bindings-test/custom-bindings
                  {:c 3}
                  "{:non"]
               f #(validate/validate-bindings x)
               {:keys [step]} (special-unwind-on-signal f :vivid.art.cli/error)]
           step))))
