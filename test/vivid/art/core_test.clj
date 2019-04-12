; Copyright 2019 Vivid Inc.

(ns vivid.art.core-test
  (:require
    [clojure.test :refer :all]
    [vivid.art.core :as art]))

(deftest plain-echo
  (testing "Plain echo pass-through"
    (is (= ""
           (art/render "")))
    (is (= "Pyramids of Mars"
           (art/render "Pyramids of Mars")))
    (is (= "色雫の洗練さ źródła বিপাক أيض"
           (art/render "色雫の洗練さ źródła বিপাক أيض"))))
  (testing "Plain echo pass-through preserves whitespace"
    (is (= "   "
           (art/render "   ")))
    (is (= " flanked    "
           (art/render " flanked    ")))
    (is (= "\t \n \t"
           (art/render "\t \n \t")))
    (is (= "\"I'm Double-Quoted\""
           (art/render "\"I'm Double-Quoted\"")))
    (is (= "<\"% > % < % %%< >%%\n\t \n<\n%\t="
           (art/render "<\"% > % < % %%< >%%\n\t \n<\n%\t=")))))

(deftest form-evaluation
  (testing "Evaluation of a form"
    (is (= ""
           (art/render "<%%>")))
    (is (= ""
           (art/render "<% nil %>")))
    (is (= ""
           (art/render "<% (+ 1 1) %>")))))

(deftest echo-form-evaluation
  (testing "Echoing form evaluation value"
    (is (= "3"
           (art/render "<%= (+ 1 2) %>")))
    (is (= "abc x = 5 exactly"
           (art/render "abc <% (def x 5) %>x = <%= x %> exactly")))))

(deftest well-formed-templates
  (testing "Ensure template examples occuring in the project README function correctly"
    (is (= "There were 3 swallows, dancing in the sky."
           (art/render "There were <%= (+ 1 2) %> swallows, dancing in the sky.")))
    (is (= "We are but stow-aways aboard a drifting ship, forsaken to the caprices of the wind and currents."
           (art/render "We are but stow-aways aboard a drifting ship, forsaken to the caprices of the wind and currents.")))
    (is (= "
<p>
Chondrichthyes research published in 1987, 1989, 1992.
</p>"
           (art/render "<%
(require '[clojure.string])
(def publication-dates [1987 1989 1992])
(defn cite-dates [xs] (clojure.string/join \", \" xs))
%>
<p>
Chondrichthyes research published in <%= (cite-dates publication-dates) %>.
</p>"))))

  (testing "Well-formed templates"
    (is (= "Pi is approximately equal to 3.14."
           (art/render "<%(def pi 3.14)%>Pi is approximately equal to <%=pi%>.")))
    (is (= "
3 + 9 = 12
Sally Forth"
           (art/render "<%
(defn appnd [s] (str s \"th\"))
(defn plus9 [x] (+ x 9))
%>
3 + 9 = <%= (plus9 3) %>
Sally <%= (appnd \"For\") %>")))
    (is (= "Countdown: 5 4 3 2 1 0"
           (art/render "Countdown:<%=(loop [s \"\"  x 5] (if (>= x 0) (recur (str s \" \" x) (dec x)) s))%>")))))
