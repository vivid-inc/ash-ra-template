; Copyright 2019 Vivid Inc.

(ns vivid.art.dependencies-test
  (:require
    [clojure.test :refer :all]
    [vivid.art :as art]))

(deftest clojure-versions
  (are [version-string]
    (= version-string
         (art/render "<%= (let [{:keys [major minor incremental]} *clojure-version*]
(format \"%d.%d.%d\" major minor incremental))%>"
                     {:dependencies {'org.clojure/clojure {:mvn/version version-string}}}))
    "1.9.0"
    "1.10.0"
    "1.10.1"))
