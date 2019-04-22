; Copyright 2019 Vivid Inc.

(ns vivid.art.examples-test
  "Confirms that ART works as promised in the README file."
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [vivid.art :as art]))

(deftest usage
  (testing "All code samples in the README file"
    (are [expected template]
      (= expected (art/render template))
      "There were 3 swallows, dancing in the sky."
      "There were <%= (+ 1 2) %> swallows, dancing in the sky."

      "There were 3 swallows, dancing in the sky."
      (slurp (io/resource "prelude.html.art"))

      "We are but stow-aways aboard a drifting ship, forsaken to the caprices of the wind and currents."
      "We are but stow-aways aboard a drifting ship, forsaken to the caprices of the wind and currents."

      ""
      "<% (def button-classes [:primary :secondary :disabled]) %>"

      "\n\n"
      "
<%
(defn updated-statement
  [date version]
  (format \"This document was updated on %s for version %s\"
          date version))
%>
")

    (is (= "
<p>
Chondrichthyes research published in 1987, 1989, 1992.
</p>
"
           (art/render "<%
(require '[clojure.string])
(def publication-dates [1987 1989 1992])
(defn cite-dates [xs] (clojure.string/join \", \" xs))
%>
<p>
Chondrichthyes research published in <%= (cite-dates publication-dates) %>.
</p>
")))

    (is (= "\n\n<li><a href=\"#739\" id=\"link\">Moving wing assembly in place</a></li><li><a href=\"#740\" id=\"link\">Connecting fuel lines and hydraulics</a></li><li><a href=\"#741\" id=\"link\">Attaching wing assembly to fuselage</a></li>\n"
           (art/render "
<%
(require '[hiccup.core])

(def ^:const toc-headings [{:id 739 :text \"Moving wing assembly in place\"}
                           {:id 740 :text \"Connecting fuel lines and hydraulics\"}
                           {:id 741 :text \"Attaching wing assembly to fuselage\"}])

(defn toc-entry [heading]
  (hiccup.core/html [:li
    [:a#link
      {:href (str \"#\" (heading :id))}
      (heading :text)]]))
%>
<%= (apply str (map toc-entry toc-headings)) %>
"
                       :dependencies {'hiccup {:mvn/version "1.0.5"}})))

    (are [expected template]
      (= expected (art/render template))
      "Splash!"
      "<% (emit \"Splash!\") %>"
      "Splash!"
      "<%= \"Splash!\" %>")))
