; Copyright 2022 Vivid Inc.

(defproject net.vivid-inc/lein-art "0.7.0"

  :note "THIS FILE IS GENERATED AUTOMATICALLY BY bin/gen.sh"
  :description "Leiningen plugin for rendering Ash Ra .art templates"
  :url "https://github.com/vivid-inc/ash-ra-template"
  :license {:distribution :repo
            :name         "Apache License 2.0"
            :url          "https://www.apache.org/licenses/LICENSE-2.0"}

  :aliases {"build"     ["do"
                         ["version"]
                         ["clean"]
                         ["eftest"]
                         ;["cloverage"]
                         ;["clj-kondo"]
                         ["jar"]
                         ["install"]]
            "clj-kondo" ["with-profile" "clj-kondo" "run" "-m" "clj-kondo.main" "--"
                         "--config" "../.clj-kondo/config.edn"
                         "--lint" "src/:test/"
                         "--parallel"]
            "gen"       ["art" "render"]
            "test"      ["with-profile" "+clojure-1.10.0:+clojure-1.10.1:+clojure-1.10.2:+clojure-1.10.3:+clojure-1.11.0:+clojure-1.11.1" "build"]}

  :art {:templates  "assets"
        :bindings   "../assets/vivid-art-facts.edn"
        ; ERB delimiters because the README utilizes lispy delimiters in explanatory text.
        :delimiters erb
        :output-dir "."}

  :cloverage {:codecov? true
              :html?    true
              :junit?   true
              :output   "cloverage"                  ; "lein jar" destroys target/cloverage
              }

  :dependencies [[net.vivid-inc/art-cli   "0.7.0"]
                 [org.clojure/tools.cli   "1.0.219"]]

  :eval-in-leiningen true

  :exclusions [org.clojure/clojure]

  :global-vars {*warn-on-reflection* true}

  :javac-options ["-target" "1.8"]

  :manifest {"Built-By" "vivid"}

  :min-lein-version "2.9.8"

  ; Enable this to assist with determining :excludes whenever dependencies and
  ; plugins change, then re-disable it.
  ;:pedantic? :abort

  :plugins [[com.github.liquidz/antq "RELEASE"]
            [lein-cljfmt    "0.9.2" :exclusions [com.fasterxml.jackson.core/jackson-core
                                              org.clojure/clojure]]
            [lein-cloverage "1.2.4"]
            [lein-eftest    "0.6.0"]
            [lein-nvd       "1.4.1" :exclusions [com.fasterxml.jackson.core/jackson-annotations
                                                 commons-io
                                                 org.apache.commons/commons-lang3
                                                 org.clojure/clojure
                                                 org.slf4j/jcl-over-slf4j
                                                 org.slf4j/slf4j-api]]]

  :profiles {:clj-kondo {:dependencies [[clj-kondo "RELEASE"]
                                        [org.clojure/clojure "1.10.0"]]}

             :clojure-1.10.0 {:dependencies [[org.clojure/clojure "1.10.0"]]}
             :clojure-1.10.1 {:dependencies [[org.clojure/clojure "1.10.1"]]}
             :clojure-1.10.2 {:dependencies [[org.clojure/clojure "1.10.2"]]}
             :clojure-1.10.3 {:dependencies [[org.clojure/clojure "1.10.3"]]}
             :clojure-1.11.0 {:dependencies [[org.clojure/clojure "1.11.0"]]}
             :clojure-1.11.1 {:dependencies [[org.clojure/clojure "1.11.1"]]}

             :dev {:dependencies   [[org.clojure/clojure "1.10.0"]
                                    ;; Diffs equality assertions in test failure output
                                    ;; https://github.com/pjstadig/humane-test-output
                                    [pjstadig/humane-test-output "0.11.0"]]

                   :injections     [(require 'pjstadig.humane-test-output)
                                    (pjstadig.humane-test-output/activate!)]

                   :plugins        [;; Reloads & re-runs tests on file changes
                                    ;; https://github.com/jakemcc/lein-test-refresh
                                    [com.jakemccrary/lein-test-refresh "0.24.1"]]

                   :test-refresh   {:quiet true}}

             :provided {:dependencies [[leiningen "2.9.8"]]}}

  :repositories [["clojars" {:sign-releases false}]])
