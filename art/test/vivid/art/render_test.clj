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

(ns vivid.art.render-test
  (:require
    [clojure.test :refer [are deftest is testing]]
    [vivid.art :as art]))

(deftest plain-echo
  (testing "Plain echo pass-through"
    (are [expected template]
      (= expected (art/render template))
      "" ""
      "Pyramids of Mars" "Pyramids of Mars"
      "色雫の洗練さ źródła বিপাক أيض" "色雫の洗練さ źródła বিপাক أيض"))
  (testing "Plain echo pass-through preserves whitespace"
    (are [expected template]
      (= expected (art/render template))
      "   " "   "
      " flanked    " " flanked    "
      "\t \n \t" "\t \n \t"
      "\"I'm Double-Quoted\"" "\"I'm Double-Quoted\""
      "<\"% > % < % %%< >%%\n\t \n<\n%\t=" "<\"% > % < % %%< >%%\n\t \n<\n%\t=")))

(deftest well-formed-templates
  (testing "Well-formed templates"
    (is (= "Pi is approximately equal to 3.14."
           (art/render "<((def pi 3.14))>Pi is approximately equal to <(=pi)>.")))
    (is (= "
3 + 9 = 12
Sally Forth"
           (art/render "<(
(defn appnd [s] (str s \"th\"))
(defn plus9 [x] (+ x 9))
)>
3 + 9 = <(= (plus9 3) )>
Sally <(= (appnd \"For\") )>")))
    (is (= "Countdown: 5 4 3 2 1 0"
           (art/render "Countdown:<(=(loop [s \"\"  x 5] (if (>= x 0) (recur (str s \" \" x) (dec x)) s)))>")))))

(deftest whitespace-preservation
  (testing "Whitespace is preserved"
    (are [expected template]
      (= expected (art/render template))
      " " " "
      "\t" "\t"
      "\n" "\n"
      " " " <(      )>"
      "\n" "<(      )>\n"
      ; Consecutive tags
      " \t\n " " <()><( )>\t\n "
      " \n\n" "<()> <(=\n\t\"\n\"\t)><(   )>\n<()>")))

(deftest quintessential-template-code-constructs
  (testing "Sequence-based HTML list"
    (is (= "<ul><li>Item 1</li><li>Item 2</li><li>Item 3</li></ul>"
           (art/render "<ul><((doseq [i (range 1 4)])><li>Item <(=i)></li><())></ul>")))))
