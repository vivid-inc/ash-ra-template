; Copyright 2024 Vivid Inc. and/or its affiliates.
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

(defproject net.vivid-inc/clj-art "0.7.2"

  :note "THIS FILE IS GENERATED AUTOMATICALLY FROM AN ART TEMPLATE BY bin/gen.sh"
  :description "Clojure tool for rendering Ash Ra .art templates"
  :license {:distribution :repo
            :name         "Apache License 2.0"
            :url          "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:dir  ".."
        :name "git"
        :tag  "0.7.2"
        :url  "https://github.com/vivid-inc/ash-ra-template"}
  :url "https://github.com/vivid-inc/ash-ra-template"

  :aliases {"build"     ["do"
                         ["version"]
                         ["clean"]
                         ["check"]
                         ["eftest"]
                         ["cloverage"]
                         ["jar"]
                         ["install"]]
            "clj-kondo" ["with-profile" "clojure-1.12.0,clj-kondo" "run" "-m" "clj-kondo.main" "--"
                         "--lint" "src:test"
                         "--parallel"]
            "lint"      ["do"
                         ["cljfmt" "check"]
                         ["clj-kondo"]
                         ["antq"]
                         ["nvd" "check"]]
            "test-all"  ["build"]}

  :cloverage {:codecov? true
              :html?    true
              :junit?   true
              :output   "cloverage"                  ; "lein jar" destroys target/cloverage
              }

  :dependencies [[net.vivid-inc/art-cli "0.7.2"]]

  :eftest {:capture-output? true}

  :exclusions [org.clojure/clojure]

  :javac-options ["-target" "null"]

  :manifest {"Built-By" "vivid"}

  :min-lein-version "2.10.0"

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

  :profiles {:clj-kondo {:dependencies [[clj-kondo "RELEASE"]]}

             :clojure-1.10.0 {:dependencies [[org.clojure/clojure "1.10.0"]]}
             :clojure-1.10.3 {:dependencies [[org.clojure/clojure "1.10.3"]]}
             :clojure-1.11.4 {:dependencies [[org.clojure/clojure "1.11.4"]]}
             :clojure-1.12.0 {:dependencies [[org.clojure/clojure "1.12.0"]]}

             :deploy {:javac-options ["-target" "1.8" "-source" "1.8"]}

             :dev       {:dependencies [[org.clojure/clojure "1.10.0"]]}}

  :repositories [["clojars" {:sign-releases false}]])
