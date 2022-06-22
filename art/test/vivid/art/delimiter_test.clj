; Copyright 2022 Vivid Inc. and/or its affiliates.
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

(ns vivid.art.delimiter-test
  (:require
    [clojure.test :refer [are deftest testing]]
    [vivid.art :as art]
    [vivid.art.delimiters]))

(deftest api-contract
  (testing "Default lispy delimiters"
    (are [expected template]
      (= expected (art/render template))
      "plain text" "plain text"
      "juniper" "juni<()>per"
      "START 1234 END" "START <((def cnt 4)(doseq [i (range 1 (inc cnt))])><(=i)><())> END"))
  (testing "Manually specify lispy delimiters"
    (are [expected template]
      (= expected (art/render template
                              :delimiters {:begin-forms "<("
                                           :begin-eval  "<(="
                                           :end-forms   ")>"}))
      "plain text" "plain text"
      "juniper" "juni<()>per"
      "START 1234 END" "START <((def cnt 4)(doseq [i (range 1 (inc cnt))])><(=i)><())> END")))

(deftest bundled-delimiter-definitions
  (testing "ART-provided delimiter library: ERB"
    (are [expected template]
      (= expected (art/render template
                              :delimiters vivid.art.delimiters/erb))
      "plain text" "plain text"
      "juniper" "juni<%%>per"
      "START 1234 END" "START <%(def cnt 4)(doseq [i (range 1 (inc cnt))]%><%=i%><%)%> END"))
  (testing "ART-provided delimiter library: Jinja"
    (are [expected template]
      (= expected (art/render template
                              :delimiters vivid.art.delimiters/jinja))
      "plain text" "plain text"
      "juniper" "juni{%%}per"
      "START 1234 END" "START {%(def cnt 4)(doseq [i (range 1 (inc cnt))]%}{{i}}{%)%} END"))
  (testing "ART-provided delimiter library: Mustache"
    (are [expected template bindings]
      (= expected (art/render template
                              :delimiters vivid.art.delimiters/mustache
                              :bindings   bindings))
      "plain text" "plain text" {}
      "juniper" "juni{{}}per" {}
      "START 1234 END" "START {{numbers}} END" '{numbers 1234}))
  (testing "ART-provided delimiter library: PHP"
    (are [expected template]
      (= expected (art/render template
                              :delimiters vivid.art.delimiters/php))
      "plain text" "plain text"
      "juniper" "juni<??>per"
      "START 1234 END" "START <?(def cnt 4)(doseq [i (range 1 (inc cnt))]?><?=i?><?)?> END")))

(deftest delimiter-syntax
  (testing "Unbalanced delimiters"
    (are [expected template]
      (= expected (art/render template))
      "Unbalanced does switchstream processing mode" "Unbalanced <( (emit (str \"does switch\" \"stream processing mode\"))"
      "Unbalanced  doesn't switch stream processing mode" "Unbalanced )> doesn't switch stream processing mode"))
  (testing "Each delimiter one-by-one"
    (are [expected template]
      (= expected (art/render template))
      "" "<("
      "" "<(="
      "" ")>"))
  (testing "Tricky syntax"
    (are [expected template]
      (= expected (art/render template))
      "<" "<"
      "abc<" "abc<<("
      ; The middle '<' evaluates to the '<' Clojure function, resulting in no template output.
      "" "<(<<(")))

(deftest pathological
  (testing "Failed at some point during development"
    (are [expected template]
      (= expected (art/render template))
      "\n" "\n")))
