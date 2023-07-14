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

(ns vivid.art.cli.bindings-test
  "Guarantee certain types can be transported across the shimdandy bridge unspoiled."
  (:require
    [clojure.test :refer [are deftest testing]]
    [vivid.art :as art]))

(deftest bindings-styles
  (testing "Quote the entire bindings map"
    (are [expected template opts]
      (= expected (apply art/render template opts))

      "" "" [:bindings '{}]))
  (testing "Quote individual bindings map keys and/or values"
    (are [expected template opts]
      (= expected (apply art/render template opts))

      "" "" [:bindings '{}])))

(deftest data-types
  (testing "Unspoiled transport of a subset of Clojure data types through pr"
    (are [expected template opts]
      (= expected (apply art/render template opts))

      ; Empty bindings
      "empty" "<(= 'empty )>" []
      "empty" "<(= 'empty )>" [:bindings '{}]

      ; Booleans
      "true class java.lang.Boolean false class java.lang.Boolean" "<(= t )> <(= (type t) )> <(= f )> <(= (type f) )>" [:bindings {'t true 'f 'false}]

      ; Characters
      "z" "<(= a )>" [:bindings '{a \z}]

      ; Collections
      "true" "<(= (coll? c) )>"                     [:bindings '{c (list 0 1 2)}]
      "true" "<(= (seq? s) )>"                      [:bindings '{s (list 0 1 2)}]
      "true (0 1 2)" "<(= (list? l) )> <(= l )>"    [:bindings '{l (list 0 1 2)}]
      "true {:a 5}" "<(= (map? m) )> <(= m )>"      [:bindings '{m {:a 5}}]
      "true #{9}" "<(= (set? s) )> <(= s )>"        [:bindings '{s #{9}}]
      "true [0 1 2]" "<(= (vector? v) )> <(= v )>"  [:bindings '{v [0 1 2]}]

      ; Functions
      "123" "<(= (my-lambda 62) )>" [:bindings '{my-lambda #(+ 61 %)}]
      "2.71828" "<(= (trunc java.lang.Math/E 7) )>" [:bindings '{trunc (fn [x n] (subs (pr-str x) 0 n))}]

      ; Integers
      "3" "<(= (+ a b) )>" [:bindings '{a 1 b 2}]

      ; Keywords
      ":im-a-keyword class clojure.lang.Keyword" "<(= (str a \" \" (type a)) )>" [:bindings '{a :im-a-keyword}]

      ; nil
      "true" "<(= (= nil a) )>" [:bindings {'a 'nil}]
      "null" "<(= (type a) )>"  [:bindings {'a 'nil}]

      ; Strings
      "Mass-produced comedy .." "Mass-produced <(= plain )> .." [:bindings '{plain "comedy"}]
      ".. is \"culture\" dependent" ".. is <(= embedded-quotes )>" [:bindings {'embedded-quotes "\"culture\" dependent"}]

      ; Symbols
      "xyz" "<(= sym )>" [:bindings '{sym 'xyz}]
      "a*b+c!d.e:f-g_h?9" "<(= all-chars )>" [:bindings '{all-chars 'a*b+c!d.e:f-g_h?9}]
      (pr-str ::namespaced-sym) "<(= with-ns )>" [:bindings {'with-ns ::namespaced-sym}])))
