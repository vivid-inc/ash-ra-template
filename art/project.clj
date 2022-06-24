; Copyright 2021 Vivid Inc.

; Interesting commands:
;
; Examine the full list of transitive dependencies
; $ lein with-profile '' deps :tree

(defproject net.vivid-inc/art "0.7.0"

  :note "THIS FILE IS GENERATED AUTOMATICALLY BY bin/gen.sh"
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
                         "--config" "../.clj-kondo/config.edn"
                         "--lint" "src/:test/"
                         "--parallel"]
            "nvd"      ["nvd" "check"]
            "test"     ["with-profile" "test" "with-profile" "+clojure-1.10.0:+clojure-1.10.1:+clojure-1.10.2:+clojure-1.10.3:+clojure-1.11.0:+clojure-1.11.1" "build"]}

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

  :min-lein-version "2.9.8"

  ; Uncomment this :pedantic? to assist with determining :excludes whenever
  ; dependencies and plugins change, then re-disable it.
  ;:pedantic? :abort

  :plugins [[com.github.liquidz/antq "RELEASE" :exclusions [commons-codec/commons-codec
                                                            org.apache.httpcomponents/httpclient
                                                            org.codehaus.plexus/plexus-utils
                                                            org.slf4j/slf4j-api]]
            [lein-cljfmt             "0.8.0"   :exclusions [com.fasterxml.jackson.core/jackson-core]]
            [lein-cloverage          "1.2.3"]
            [lein-eftest             "0.5.9"]
            [lein-nvd                "1.4.1"   :exclusions [com.fasterxml.jackson.core/jackson-annotations
                                                            commons-io
                                                            org.apache.commons/commons-lang3
                                                            org.codehaus.plexus/plexus-utils
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

             :dev       {:dependencies   [[org.clojure/clojure "1.10.0"]
                                          ;; Diffs equality assertions in test failure output
                                          ;; https://github.com/pjstadig/humane-test-output
                                          [pjstadig/humane-test-output "0.11.0"]]

                         :injections     [(require 'pjstadig.humane-test-output)
                                          (pjstadig.humane-test-output/activate!)

                                          ; Keep Spec enabled in the context of development of ART,
                                          ; but not in the shipping jar.
                                          ;
                                          ; Load all namespaces in the project + Spec
                                          (require 'vivid.art)
                                          (require 'clojure.spec.test.alpha)
                                          ; Instrument everything Spec can find
                                          (clojure.spec.test.alpha/instrument)]

                         :plugins        [;; Reloads & re-runs tests on file changes
                                          ;; https://github.com/jakemcc/lein-test-refresh
                                          [com.jakemccrary/lein-test-refresh "0.25.0"]]

                         :resource-paths ["test-resources" "../examples"]

                         :test-refresh   {:quiet true}}

             :test {:dependencies [[org.clojure/core.async "1.5.648"]]}}

  :repositories [["clojars" {:sign-releases false}]])
