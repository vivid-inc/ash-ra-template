; Copyright 2020 Vivid Inc.

(defproject vivid/clj-art "0.6.0"

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
                         ["clj-kondo"]
                         ["jar"]
                         ["install"]]
            "clj-kondo" ["with-profile" "clj-kondo" "run" "-m" "clj-kondo.main" "--"
                         "--lint" "src/"]
            "nvd"       ["nvd" "check"]
            "test-all"  ["build"]}

  :cloverage {:codecov? true
              :html?    true
              :junit?   true
              :output   "cloverage"                  ; "lein jar" destroys target/cloverage
              }

  :dependencies [[org.clojure/tools.cli "1.0.206"]
                 [special "0.1.3-Beta1"]
                 [vivid/art "0.6.0"]]

  :dev-dependencies [[org.clojure/clojure "1.9.0"]]

  :exclusions [org.clojure/clojure]

  :global-vars {*warn-on-reflection* true}

  :javac-options ["-target" "1.8"]

  :manifest {"Built-By" "vivid"}

  :min-lein-version "2.9.1"

  ; Enable this to assist with determining :excludes whenever dependencies and
  ; plugins change, then re-disable it.
  ;:pedantic? :abort

  :plugins [[lein-ancient "0.6.15"]
            [lein-cljfmt "0.7.0"]
            [lein-cloverage "1.2.2"]
            [lein-eftest "0.5.9"]
            [lein-nvd "1.4.1" :exclusions [com.fasterxml.jackson.core/jackson-annotations
                                           commons-io
                                           org.apache.commons/commons-lang3
                                           org.clojure/clojure
                                           org.slf4j/jcl-over-slf4j
                                           org.slf4j/slf4j-api]]]

  :profiles {:clj-kondo {:dependencies [[org.clojure/clojure "1.9.0"]
                                        [clj-kondo "RELEASE"]]}
             :dev       {:dependencies [[org.clojure/clojure "1.9.0"]]}}

  :repositories [["clojars" {:sign-releases false}]])
