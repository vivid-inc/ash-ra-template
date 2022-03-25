(defproject example-override-clojure-version "0"

  :plugins [[net.vivid-inc/lein-art "0.7.0"]]

  :art {:templates    "templates"
        :dependencies {org.clojure/clojure {:mvn/version "1.10.1"}}
        :output-dir   "target"})
