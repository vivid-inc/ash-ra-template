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

(ns vivid.art.public-api-test
  (:require
    [clojure.test :refer [are deftest is testing]]
    [vivid.art :as art]
    [vivid.art.delimiters]
    [vivid.art.failure])
  (:import
    (clojure.lang ArityException)))

(deftest blank-input
  (is (= nil (art/render nil)) "ART produces nil output in response to nil input")
  (is (= "" (art/render "")) "ART produces empty string output in response to empty string input"))

(deftest delimiters
  (testing "Delimiters default to lispy"
    (is (= "Quite tasty."
           (art/render "Quite <( (def acorn \"tasty\") )><(= acorn )>.")))
    (are [template delimiters]
      (= "Quite tasty." (art/render template
                                    {:delimiters delimiters}))
      "Quite <% (def acorn \"tasty\") %><%= acorn %>." vivid.art.delimiters/erb
      "Quite <? (def acorn \"tasty\") ?><?= acorn ?>." vivid.art.delimiters/php
      "Quite #( (def acorn \"tasty\") )##(= acorn )#." {:begin-forms "#("
                                                        :end-forms   ")#"
                                                        :begin-eval  "#(="}
      "Quite AB (def acorn \"tasty\") YZABC acorn YZ." {:begin-forms "AB"
                                                        :end-forms   "YZ"
                                                        :begin-eval  "ABC"})))

(deftest echo-form-evaluation
  (testing "Echoing form evaluation value"
    (are [expected template]
      (= expected (art/render template))
      "3" "<(= (+ 1 2) )>"
      "abc x = 5 exactly" "abc <( (def x 5) )>x = <(= x )> exactly")))

(deftest emit-evaluation
  (testing "Evaluation of (emit)"
    (are [expected template]
      (= expected (art/render template))
      ; No arguments
      "" "<((emit))>"
      ; Simple clojure form
      "2.718281" "<((emit (str 2.718281)))>"
      ; Unnecessarily qualified (emit) within the namespace.
      "2.718281" "<((user/emit (str 2.718281)))>"))
  (testing "Namespace qualification regarding (emit)"
    (are [expected template]
      (= expected (art/render template))
      ; Change to another ns, and use qualified (emit).
      "Sesame" "<( (ns emit-test-4FA0BF32) (user/emit \"Sesame\") )>"
      ; Change to another ns and then back again, and use unqualified (emit).
      "zigzag" "<( (ns flip-flop-71D1C341) (ns user) (emit 'zigzag) )>")
    (is (thrown? Throwable
                 ; Change to another ns and use unqualified and undefined (emit), expecting an error.
                 (art/render "<( (ns unq) (emit 'unqualified) )>")))))

(deftest failures
  (is (art/failure? (vivid.art.failure/make-failure :test-generated-failure-type
                                                    {}
                                                    ""))
      "ART recognizes a failure produced by itself"))

(deftest form-evaluation
  (testing "Evaluation of arbitrary Clojure forms"
    (are [expected template]
      (= expected (art/render template))
      "" "<()>"
      "" "<(   \t \n  )>"
      "" "<( nil )>"
      "" "<( 7 9 )>"
      "" "<( (+ 1 1) )>"
      "" "<(:a (apply + (range 10)) 0 nil true (def psuedo-nil nil))>")))

(defn get-root-cause
  [^Throwable t]
  (let [cause (.getCause t)]
    (if cause
      (get-root-cause cause)
      t)))

(deftest function-arity
  (testing "(failure?) invalid arity"
    (are [args]
      (thrown? ArityException (apply art/failure? args))
      []
      [0 1]
      [0 1 2]
      [0 1 2 3]))
  (testing "(render) invalid arity"
    (is (thrown? Throwable (apply art/render [])))))

(deftest namespace-rules
  (testing "Initial namespace"
    (are [expected template]
      (= expected (art/render template))
      "user" "<(=(str (ns-name *ns*)))>"
      "not-the-initial-art-namespace-41C0AF0F" "<( (ns not-the-initial-art-namespace-41C0AF0F) (user/emit (str (ns-name *ns*))) )>"))
  (testing "Well-formed ART template using additional namespace"
    (is (= "Fibonacci(60) = 1548008755920"
           (art/render
             "<(
             (ns fibonacci-72B642F6)
             (def sqrt-5 (Math/sqrt 5))
             (def phi (/ (+ 1 sqrt-5) 2))
             (defn fib [n] (long (Math/round (/ (Math/pow phi n) sqrt-5))))
             )><(
             (ns user)
             (def n 60)
             )><(= (format \"Fibonacci(%d) = %s\" n (fibonacci-72B642F6/fib n)))>")))))

(deftest template-code-constructs
  (testing "Clojure comments"
    (are [expected template]
      (= expected (art/render template))
      "" "<(=)>"
      "" "<( #_(emit 123) )>"
      "123" "<( ;(emit \"abc\"))><( (emit 123) )>")))
