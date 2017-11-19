(ns vivid.art.core-test
  (:require [clojure.test :refer :all]
            [vivid.art.core :refer :all]))

(deftest primitive-echo-pass-through
  (testing "Plain pass-through"
    (is (= ""
           (render "")))
    (is (= "Pyramids of Mars"
           (render "Pyramids of Mars")))
    (is (= "色雫の洗練さ źródła বিপাক أيض"
           (render "色雫の洗練さ źródła বিপাক أيض"))))
  (testing "Echo pass-through preserves whitespace"
    (is (= ""
           (render "")))
    (is (= "   "
           (render "   ")))
    (is (= " flanked    "
           (render " flanked    ")))
    (is (= "\t \n \t"
           (render "\t \n \t")))
    (is (= "\"I'm Double-Quoted\""
           (render "\"I'm Double-Quoted\"")))
    (is (= "<\"% > % < % %%< >%%\n\t \n<\n%\t="
           (render "<\"% > % < % %%< >%%\n\t \n<\n%\t=")))))

(deftest primitive-eval-forms
  (testing "Evaluating forms"
    (is (= ""
           (render "<%%>")))
    (is (= ""
           (render "<% nil %>")))
    (is (= ""
           (render "<% (+ 1 1) %>")))))

(comment deftest primitive-echo-eval-forms
  (testing "Echoing the results of evaluating forms"
           (comment is (= "3"
                          (render "<%= (+ 1 2) %>")))
           (comment is (= "abc x = 5 exactly"
                          (render "abc <% (def x 5) %> x = <%= x %> exactly")))))
