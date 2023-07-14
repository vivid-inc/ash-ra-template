; Copyright 2020 Vivid Inc.

(defproject net.vivid-inc/clj-art "0.7.0"

  :note "THIS FILE IS GENERATED AUTOMATICALLY BY bin/gen.sh"
  :description "Clojure tool for rendering Ash Ra .art templates"
  :url "https://github.com/vivid-inc/ash-ra-template"
  :license {:distribution :repo
            :name         "Apache License 2.0"
            :url          "https://www.apache.org/licenses/LICENSE-2.0"}

  :aliases {"build"     ["do"
                         ["version"]
                         ["clean"]
                         ["eftest"]
                         ["cloverage"]
                         ["jar"]
                         ["install"]]
            "clj-kondo" ["with-profile" "clj-kondo" "run" "-m" "clj-kondo.main" "--"
                         "--config" "../.clj-kondo/config.edn"
                         "--lint" "src/:test/"
                         "--parallel"]
            "nvd"       ["nvd" "check"]
            "test-all"  ["build"]}

  :cloverage {:codecov? true
              :html?    true
              :junit?   true
              :output   "cloverage"                  ; "lein jar" destroys target/cloverage
              }

  :dependencies [[net.vivid-inc/art-cli "0.7.0"]]

  :exclusions [org.clojure/clojure]

  :global-vars {*warn-on-reflection* true}

  :javac-options ["-target" "1.8"]

  :manifest {"Built-By" "vivid"}

  :min-lein-version "2.9.8"

  ; Enable this to assist with determining :excludes whenever dependencies and
  ; plugins change, then re-disable it.
  ;:pedantic? :abort

  :plugins [[com.github.liquidz/antq "RELEASE"]
            [lein-cljfmt             "0.9.2"]
            [lein-cloverage          "1.2.4"]
            [lein-eftest             "0.6.0"]
            [lein-nvd                "1.4.1" :exclusions [com.fasterxml.jackson.core/jackson-annotations
                                                          commons-io
                                                          org.apache.commons/commons-lang3
                                                          org.clojure/clojure
                                                          org.slf4j/jcl-over-slf4j
                                                          org.slf4j/slf4j-api]]]

  :profiles {:clj-kondo {:dependencies [[clj-kondo "RELEASE"]
                                        [org.clojure/clojure "1.10.0"]]}

             :dev       {:dependencies [[org.clojure/clojure "1.10.0"]]}}

  :repositories [["clojars" {:sign-releases false}]])
