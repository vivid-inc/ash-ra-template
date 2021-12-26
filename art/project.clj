; Copyright 2020 Vivid Inc.

(defproject
  vivid/art "0.6.0"

  :note "THIS FILE IS AUTOMATICALLY GENERATED BY bin/gen.sh"
  :description "Ash Ra Template: Expressive and customizable template system featuring Clojure language processing."
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
                         "--lint" "src/"]
            "nvd"      ["nvd" "check"]
            "test"     ["with-profile" "+clojure-1.9.0:+clojure-1.10.0:+clojure-1.10.1:+clojure-1.10.2:+clojure-1.10.3" "build"]}

  :cloverage {:codecov? true
              :html?    true
              :junit?   true
              :output   "cloverage"                  ; "lein jar" destroys target/cloverage
              }

  :dependencies [[instaparse/instaparse "1.4.10" :exclusions [org.clojure/spec.alpha]]
                 [org.clojure/tools.deps.alpha "0.9.857" :exclusions [commons-logging/commons-logging
                                                                      org.clojure/clojure
                                                                      org.clojure/data.json
                                                                      org.clojure/tools.cli
                                                                      org.slf4j/slf4j-api]]
                 [org.projectodd.shimdandy/shimdandy-api "1.2.1"]
                 [org.projectodd.shimdandy/shimdandy-impl "1.2.1"]
                 [org.xeustechnologies/jcl-core "2.8"]
                 [reduce-fsm/reduce-fsm "0.1.4"]
                 [special/special "0.1.3-Beta1"]] ; TODO Replace with Farolero

  :exclusions [org.clojure/clojure]

  :global-vars {*warn-on-reflection* true}

  :javac-options ["-target" "1.8"]

  :manifest {"Built-By" "vivid"}

  :min-lein-version "2.9.1"

  ; Uncomment this :pedantic? to assist with determining :excludes whenever
  ; dependencies and plugins change, then re-disable it.
  ;:pedantic? :abort

  :plugins [[lein-ancient "0.6.15"]
            [lein-cljfmt "0.7.0" :exclusions [com.fasterxml.jackson.core/jackson-core
                                              org.clojure/clojure]]
            [lein-cloverage "1.2.2"]
            [lein-eftest "0.5.9"]
            [lein-ns-dep-graph "0.2.0-SNAPSHOT" :exclusions [org.clojure/clojure]]
            [lein-nvd "1.4.1" :exclusions [com.fasterxml.jackson.core/jackson-annotations
                                           commons-io
                                           org.apache.commons/commons-lang3
                                           org.clojure/clojure
                                           org.slf4j/jcl-over-slf4j
                                           org.slf4j/slf4j-api]]]

  :profiles {:clj-kondo      {:dependencies [[org.clojure/clojure "1.9.0"]
                                             [clj-kondo "RELEASE"]]},
             :clojure-1.10.0 {:dependencies [[org.clojure/clojure "1.10.0"]]},
             :clojure-1.10.1 {:dependencies [[org.clojure/clojure "1.10.1"]]},
             :clojure-1.10.2 {:dependencies [[org.clojure/clojure "1.10.2"]]},
             :clojure-1.10.3 {:dependencies [[org.clojure/clojure "1.10.3"]]},
             :clojure-1.9.0  {:dependencies [[org.clojure/clojure "1.9.0"]]},
             :dev            {:dependencies   [[pjstadig/humane-test-output "0.10.0"]],
                              :injections     [(require (quote pjstadig.humane-test-output))
                                               (pjstadig.humane-test-output/activate!)
                                               (require (quote vivid.art))
                                               (require (quote clojure.spec.test.alpha))
                                               (clojure.spec.test.alpha/instrument)],
                              :plugins        [[com.jakemccrary/lein-test-refresh
                                                "0.24.1"]],
                              :resource-paths ["test-resources" "../examples"],
                              :test-refresh   {:quiet true}}}

  :repositories [["clojars" {:sign-releases false}]])
