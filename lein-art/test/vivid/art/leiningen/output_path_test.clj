; Copyright 2019 Vivid Inc.

(ns vivid.art.leiningen.output-path-test
  (:import
    [java.io File])
  (:require
    [clojure.test :refer :all]
    [vivid.art.leiningen.exec]))

(deftest output-paths
  (are [template-base template-path output-dir expected-output-path]
    (= expected-output-path
       (str (vivid.art.leiningen.exec/template-output-path (File. template-base)
                                                           (File. template-path)
                                                           output-dir)))
    "a.csv.art" "a.csv.art" "target" "target/a.csv"
    "/a.csv.art" "/a.csv.art" "target" "target/a.csv"

    "templates" "templates/b.txt.art" "target" "target/b.txt"
    "templates/" "templates/b.txt.art" "target" "target/b.txt"

    "/templates" "/templates/c.html.art" "target" "target/c.html"
    "/templates/" "/templates/c.html.art" "target" "target/c.html"

    "site/source" "site/source/about/d.properties.art" "rendered" "rendered/about/d.properties"
    "site/source" "site/source/about/d.properties.art" "/rendered" "/rendered/about/d.properties"

    "a/b/c" "a/b/c/d/e/f/g/h.sql.art" "out" "out/d/e/f/g/h.sql"
    "a/b/c/d/e" "a/b/c/d/e/recipe.xml.art" "/out" "/out/recipe.xml"))
