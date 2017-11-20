(ns vivid.art.core-test
  (:require [clojure.test :refer :all]
            [vivid.art.core :refer :all]))

(deftest plain-echo
  (testing "Plain echo pass-through"
    (is (= ""
           (render "")))
    (is (= "Pyramids of Mars"
           (render "Pyramids of Mars")))
    (is (= "色雫の洗練さ źródła বিপাক أيض"
           (render "色雫の洗練さ źródła বিপাক أيض"))))
  (testing "Plain echo pass-through preserves whitespace"
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

(deftest form-evaluation
  (testing "Evaluation of a form"
    (is (= ""
           (render "<%%>")))
    (is (= ""
           (render "<% nil %>")))
    (is (= ""
           (render "<% (+ 1 1) %>"))))

  (testing "Echoing the results of evaluating forms"
    (is (= "3"
           (render "<%= (+ 1 2) %>")))
    (is (= "abc x = 5 exactly"
           (render "abc <% (def x 5) %>x = <%= x %> exactly")))))

(deftest echo-form-evaluation
  (testing "Echoing form evaluation value"
    (is (= ""
           (render "")))))

(deftest well-formed-templates
  (testing "Well-formed templates"
    (is (= "Pi is approximately equal to 3.14."
           (render "<%(def pi 3.14)%>Pi is approximately equal to <%=pi%>.")))
    (is (= "

<p>
This research in the field of Chondrichthyes was published in 1987, 1989, 1992.
</p>"
           (render "<%
(def publication_dates [1987 1989 1992])
(defn join [sep xs]
  (apply str (interpose sep xs)))
%>

<p>
This research in the field of Chondrichthyes was published in <%= (join \", \" publication_dates) %>.
</p>")))
    (is (= "Countdown: 5 4 3 2 1 0"
           (render "Countdown:<%=(loop [s \"\"  x 5] (if (>= x 0) (recur (str s \" \" x) (dec x)) s))%>")))))

(deftest malformed-templates
  (comment testing "TODO"
    (is (= ""
           (render "(+ 1 1) %>")))))                        ; TODO Raise a parsing error: A well-detailed error message in debug mode, or indication of failure during runtime.
