; Copyright 2019 Vivid Inc.

(ns vivid.art.api-contract-test
  (:require
    [clojure.test :refer :all]
    [vivid.art :as art]))

; TODO "<"
; TODO "abc<<<%"
; TODO "<%<%"

(deftest blank-input
  (is (= nil (art/render nil)) "ART produces nil output in response to nil input")
  (is (= "" (art/render "")) "ART produces empty string output in response to empty string input"))

(deftest echo-form-evaluation
  (testing "Echoing form evaluation value"
    (are [expected template]
      (= expected (art/render template))
      "3" "<%= (+ 1 2) %>"
      "abc x = 5 exactly" "abc <% (def x 5) %>x = <%= x %> exactly")))

(deftest emit-evaluation
  (testing "Evaluation of (emit)"
    (are [expected template]
      (= expected (art/render template))
      ; TODO (emit) with no args, expecting an error.
      ; TODO (emit) with too many args, expecting an error.
      "2.718281" "<%(emit (str 2.718281))%>"
      ; Unnecessarily qualified (emit) within the namespace.
      "2.718281" "<%(user/emit (str 2.718281))%>"))
  (testing "Namespace qualification regarding (emit)"
    (are [expected template]
      (= expected (art/render template))
      ; Change to another ns, and use qualified (emit).
      "Sesame" "<% (ns emit-test-4FA0BF32) (user/emit \"Sesame\") %>"
      ; TODO Change to another ns and use unqualified and undefined (emit), expecting an error.
      ; Change to another ns and then back again, and use unqualified (emit).
      "zigzag" "<% (ns flip-flop-71D1C341) (ns user) (emit 'zigzag) %>")))

(deftest form-evaluation
  (testing "Evaluation of arbitrary Clojure forms"
    (are [expected template]
      (= expected (art/render template))
      "" "<%%>"
      "" "<%   \t \n  %>"
      "" "<% nil %>"
      "" "<% 7 9 %>"
      "" "<% (+ 1 1) %>"
      "" "<%:a (apply + (range 10)) 0 nil true (def psuedo-nil nil)%>")))

(deftest namespace-rules
  (testing "Initial namespace"
    (are [expected template]
      (= expected (art/render template))
      "user" "<%=(str (ns-name *ns*))%>"
      "not-the-initial-art-namespace-41C0AF0F" "<% (ns not-the-initial-art-namespace-41C0AF0F) (user/emit (str (ns-name *ns*))) %>"))
  (testing "Well-formed ART template using additional namespace"
    (is (= "Fibonacci(60) = 1548008755920"
           (art/render
             "<%
             (ns fibonacci-72B642F6)
             (def sqrt-5 (Math/sqrt 5))
             (def phi (/ (+ 1 sqrt-5) 2))
             (defn fib [n] (long (Math/round (/ (Math/pow phi n) sqrt-5))))
             %><%
             (ns user)
             (def n 60)
             %><%= (format \"Fibonacci(%d) = %s\" n (fibonacci-72B642F6/fib n))%>")))))

(deftest template-code-constructs
  (testing "Clojure comments"
    (are [expected template]
      (= expected (art/render template))
      "" "<%=%>"
      "" "<% #_(emit 123) %>"
      "123" "<% ;(emit \"abc\")%><% (emit 123) %>")))
