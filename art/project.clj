; Copyright 2021 Vivid Inc.

; Interesting commands:
;
; Examine the full list of transitive dependencies
; $ lein with-profile '' deps :tree

(defproject
  net.vivid-inc/art "0.7.0"

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
            "test"     ["with-profile" "+clojure-1.10.0:+clojure-1.10.1:+clojure-1.10.2:+clojure-1.10.3:+clojure-1.11.0" "build"]}

  :cloverage {:codecov? true
              :html?    true
              :junit?   true
              :output   "cloverage"                  ; "lein jar" destroys target/cloverage
              }

  :dependencies [[instaparse/instaparse "1.4.10" :exclusions [org.clojure/spec.alpha]]
                 [reduce-fsm/reduce-fsm "0.1.4"]
                 [org.suskalo/farolero "1.4.3"]]

  :exclusions [org.clojure/clojure]

  :global-vars {*warn-on-reflection* true}

  :javac-options ["-target" "1.8"]

  :manifest {"Built-By" "vivid"}

  :min-lein-version "2.9.1"

  ; Uncomment this :pedantic? to assist with determining :excludes whenever
  ; dependencies and plugins change, then re-disable it.
  ;:pedantic? :abort

  :plugins [[com.github.liquidz/antq "RELEASE"]
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

  :profiles {:clj-kondo      {:dependencies [[org.clojure/clojure "1.10.0"]
                                             [clj-kondo "RELEASE"]]},
             :clojure-1.10.0 {:dependencies [[org.clojure/clojure "1.10.0"]]},
             :clojure-1.10.1 {:dependencies [[org.clojure/clojure "1.10.1"]]},
             :clojure-1.10.2 {:dependencies [[org.clojure/clojure "1.10.2"]]},
             :clojure-1.10.3 {:dependencies [[org.clojure/clojure "1.10.3"]]},
             :clojure-1.11.0 {:dependencies [[org.clojure/clojure "1.11.0"]]},
             :dev            {:dependencies   [[pjstadig/humane-test-output "0.11.0"]],
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
