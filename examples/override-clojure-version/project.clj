(defproject example-override-clojure-version "0"

  :plugins [[vivid/lein-art "0.6.0"]]

  :art {:templates    "templates"
        :dependencies {org.clojure/clojure {:mvn/version "1.10.1"}}
        :output-dir   "target"})
