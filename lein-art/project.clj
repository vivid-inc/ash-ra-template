; Copyright 2019 Vivid Inc.

(defproject vivid/lein-art "0.5.0"

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
                         "--lint" "src/"]
            "mkdocs"    ["art"]
            "test"      ["with-profile" "+clojure-1.9.0:+clojure-1.10.0:+clojure-1.10.1" "build"]}

  :art {:templates  "assets/README.md.art"
        :bindings   "../assets/vivid-art-facts.edn"
        :delimiters "{:begin-forms \"{%\" :end-forms \"%}\" :begin-eval \"{%=\" :end-eval \"%}\"}"
        :output-dir "."}

  :cloverage {:codecov? true
              :html?    true
              :junit?   true
              :output   "cloverage"                  ; "lein jar" destroys target/cloverage
              }

  :dependencies [[org.clojure/tools.cli "1.0.194"]
                 [vivid/art "0.5.0"]]

  :dev-dependencies [[org.clojure/clojure "1.9.0"]
                     [leiningen "2.9.1"]]

  :eval-in-leiningen true

  :exclusions [org.clojure/clojure]

  :global-vars {*warn-on-reflection* true}

  :javac-options ["-target" "1.8"]

  :manifest {"Built-By" "vivid"}

  ; This version of Leiningen is what we have available to us in CI.
  :min-lein-version "2.9.1"

  ; Enable this to assist with determining :excludes whenever dependencies and
  ; plugins change, then re-disable it.
  ;:pedantic? :abort

  :plugins [[lein-ancient "0.6.15"]
            [lein-cljfmt "0.7.0" :exclusions [com.fasterxml.jackson.core/jackson-core
                                              org.clojure/clojure]]
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

             :clojure-1.10.0 {:dependencies [[org.clojure/clojure "1.10.0"]]},
             :clojure-1.10.1 {:dependencies [[org.clojure/clojure "1.10.1"]]},
             :clojure-1.9.0  {:dependencies [[org.clojure/clojure "1.9.0"]]},

             :dev {:dependencies   [;; Diffs equality assertions in test failure output
                                    ;; https://github.com/pjstadig/humane-test-output
                                    [pjstadig/humane-test-output "0.10.0"]]

                   :injections     [(require 'pjstadig.humane-test-output)
                                    (pjstadig.humane-test-output/activate!)]

                   :plugins        [;; Reloads & re-runs tests on file changes
                                    ;; https://github.com/jakemcc/lein-test-refresh
                                    [com.jakemccrary/lein-test-refresh "0.24.1"]]

                   :test-refresh   {:quiet true}}}

  :repositories [["clojars" {:sign-releases false}]])
