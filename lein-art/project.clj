; Copyright 2026 Vivid Inc. and/or its affiliates.
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

(defproject net.vivid-inc/lein-art "0.8.0"

  :note "THIS FILE IS GENERATED AUTOMATICALLY FROM AN ART TEMPLATE WITH bin/gen.sh"
  :description "Leiningen plugin for rendering Ash Ra .art templates"
  :license {:distribution :repo
            :name         "Apache License 2.0"
            :url          "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:dir  ".."
        :name "git"
        :tag  "0.8.0"
        :url  "https://github.com/vivid-inc/ash-ra-template"}
  :url "https://github.com/vivid-inc/ash-ra-template"

  :aliases {"build"     ["do"
                         ["version"]
                         ["clean"]
                         ["check"]
                         ["eftest"]
                         ; TODO Fails, due perhaps in relation to :eval-in-leiningen ["cloverage"]
                         ["jar"]
                         ["install"]]
            "clj-kondo" ["with-profile" "clojure-1.12.4,clj-kondo" "run" "-m" "clj-kondo.main" "--"]
            "gen"       ["art" "render"]
            "lint"      ["do"
                         ["cljfmt" "check"]
                         ;["clj-kondo" "--lint" "src:test"   ; ~#(clojure.string/join ":" (leiningen.core.classpath/get-classpath %))
                         ; "--dependencies" "--copy-configs" "--skip-lint"]
                         ["clj-kondo" "--lint" "src:test" "--parallel"]
                         ["antq"]
                         ["nvd" "check"]]
            "test"      ["with-profile" "+clojure-1.10.0:+clojure-1.10.3:+clojure-1.11.4:+clojure-1.12.4" "build"]}

  :art {:templates  "assets"
        :bindings   "../assets/vivid-art-facts.edn"
        ; ERB delimiters because the README utilizes lispy delimiters in explanatory text.
        :delimiters erb
        :output-dir "."}

  ;:cloverage {:codecov? true
  ;            :html?    true
  ;            :junit?   true
  ;            :output   "cloverage"                  ; "lein jar" destroys target/cloverage
  ;            }

  :dependencies [[net.vivid-inc/art-cli   "0.7.2"]
                 [org.clojure/tools.cli   "1.1.230"]]

  :eftest {:capture-output? true}

  :eval-in-leiningen true

  :exclusions [org.clojure/clojure]

  :global-vars {*warn-on-reflection* true}

  :javac-options ["-target" "null"]

  :manifest {"Built-By" "vivid"}

  :min-lein-version "2.10.0"

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

  :profiles {:clj-kondo {:dependencies [[clj-kondo "RELEASE"]]}

             :clojure-1.10.0 {:dependencies [[org.clojure/clojure "1.10.0"]]}
             :clojure-1.10.3 {:dependencies [[org.clojure/clojure "1.10.3"]]}
             :clojure-1.11.4 {:dependencies [[org.clojure/clojure "1.11.4"]]}
             :clojure-1.12.4 {:dependencies [[org.clojure/clojure "1.12.4"]]}

             ;:cloverage      {:dependencies [[leiningen "2.9.8"]
             ;                                [org.clojure/tools.namespace "1.0.0"]]}

             :deploy {:javac-options ["-target" "1.8" "-source" "1.8"]}

             :dev {:dependencies   [[org.clojure/clojure "1.10.0"]
                                    ;; Diffs equality assertions in test failure output
                                    ;; https://github.com/pjstadig/humane-test-output
                                    [pjstadig/humane-test-output "0.11.0"]]

                   :injections     [(require 'pjstadig.humane-test-output)
                                    (pjstadig.humane-test-output/activate!)]

                   :plugins        [;; Reloads & re-runs tests on file changes
                                    ;; https://github.com/jakemcc/lein-test-refresh
                                    [com.jakemccrary/lein-test-refresh "0.26.0"]]

                   :test-refresh   {:quiet true}}

             :provided {:dependencies [[leiningen "2.10.0"]]}}

  :repositories [["clojars" {:sign-releases false}]])
