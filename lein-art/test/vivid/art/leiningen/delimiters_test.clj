; Copyright 2019 Vivid Inc.

(ns vivid.art.leiningen.delimiters-test
  (:require
    [clojure.test :refer :all]
    [vivid.art.leiningen.resolve]
    [vivid.art.leiningen.cli]))

(def custom-delimiters
  {:begin-forms "{%"
   :end-forms   "%}"
   :begin-eval  "{{"
   :end-eval    "}}"})

(deftest delimiter-resolution
  (testing "Resolving delimiters by string representations of Clojure vars"
    (are [expected s]
      (= expected (vivid.art.leiningen.resolve/resolve-delimiters s))
      nil nil
      nil "non-existent"
      vivid.art.delimiters/erb "erb"
      vivid.art.delimiters/jinja "jinja"
      vivid.art.delimiters/mustache "vivid.art.delimiters/mustache"
      vivid.art.delimiters/php "vivid.art.delimiters/php"
      nil "vivid.art.delimiters/non-existent"))
  (testing "Resolving delimiters by EDN string representations of Clojure map literals"
    (are [expected s]
      (= expected (vivid.art.leiningen.resolve/resolve-delimiters s))
      nil nil
      nil "nonsense"
      nil "  {:non-sense} "
      custom-delimiters (pr-str custom-delimiters))))
