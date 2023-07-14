; Copyright 2023 Vivid Inc. and/or its affiliates.
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     https://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(defproject net.vivid-inc/art-cli "0.7.0"

  :note "THIS FILE IS GENERATED AUTOMATICALLY BY bin/gen.sh"
  :description "art-cli aggregates code common to the translation and processing of Ash Ra Template command line parameters into parameters for ART's Clojure API."
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
            "clj-kondo" ["with-profile" "clojure-1.11.1,clj-kondo" "run" "-m" "clj-kondo.main" "--"
                         "--lint" "src:test"
                         "--parallel"]
            "lint"      ["do"
                         ["cljfmt" "check"]
                         ["clj-kondo"]
                         ["antq"]
                         ["nvd" "check"]]
            "test"     ["with-profile" "+clojure-1.10.0:+clojure-1.10.1:+clojure-1.10.2:+clojure-1.10.3:+clojure-1.11.0:+clojure-1.11.1" "build"]}

  :cloverage {:codecov? true
              :html?    true
              :junit?   true
              :output   "cloverage"                  ; "lein jar" destroys target/cloverage
              }

  :dependencies [[clj-commons/pomegranate  "1.2.23"]
                 [com.nextjournal/beholder "1.0.2"]
                 [net.vivid-inc/art        "0.7.0"]
                 [org.clojure/data.json    "2.4.0"]
                 [org.clojure/tools.cli    "1.0.219"]]

  :eftest {:capture-output? true}

  :exclusions [org.clojure/clojure]

  :global-vars {*warn-on-reflection* true}

  :javac-options ["-target" "null"]

  :manifest {"Built-By" "vivid"}

  :min-lein-version "2.9.8"

  ; Enable this to assist with determining :excludes whenever dependencies and
  ; plugins change, then re-disable it.
  ;:pedantic? :abort

  :plugins [[com.github.liquidz/antq "RELEASE" :exclusions [org.apache.httpcomponents/httpclient
                                                            org.slf4j/slf4j-api
                                                            org.codehaus.plexus/plexus-utils
                                                            commons-codec]]
            [lein-cljfmt             "0.9.2"   :exclusions [commons-codec]]
            [lein-cloverage          "1.2.4"]
            [lein-eftest             "0.6.0"]
            [lein-nvd                "1.4.1"   :exclusions [com.fasterxml.jackson.core/jackson-annotations
                                                            commons-io
                                                            org.apache.commons/commons-lang3
                                                            org.codehaus.plexus/plexus-utils
                                                            org.slf4j/jcl-over-slf4j
                                                            org.slf4j/slf4j-api]]]

  :profiles {:clj-kondo {:dependencies [[clj-kondo "RELEASE"]]}

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
                                          (require 'clojure.spec.test.alpha)
                                          (require 'vivid.art)
                                          ; Instrument everything Spec can find
                                          (clojure.spec.test.alpha/instrument)]

                         :plugins        [;; Reloads & re-runs tests on file changes
                                          ;; https://github.com/jakemcc/lein-test-refresh
                                          [com.jakemccrary/lein-test-refresh "0.25.0"]]

                         :resource-paths ["test-resources" "../examples"]

                         :test-refresh   {:quiet true}}

            :provided {:dependencies [[leiningen "2.9.8"]]}}

  :repositories [["clojars" {:sign-releases false}]])
