; Copyright 2019 Vivid Inc.

(ns vivid.art.render-test
  (:require
    [clojure.test :refer :all]
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

(deftest whitespace-preservation
  (testing "Whitespace is preserved"
    (are [expected template]
      (= expected (art/render template))
      " " " "
      "\t" "\t"
      "\n" "\n"
      " " " <%      %>"
      "\n" "<%      %>\n"
      ; Consecutive tags
      " \t\n " " <%%><% %>\t\n "
      " \n\n" "<%%> <%=\n\t\"\n\"\t%><%   %>\n<%%>")))
