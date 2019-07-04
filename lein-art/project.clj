; Copyright 2019 Vivid Inc.

(defproject vivid/lein-art "0.4.0"

  :description "Leiningen plugin for rendering Ash Ra Template .art templates."
  :url "https://github.com/vivid/ash-ra-template"
  :license {:distribution :repo
            :name         "Eclipse Public License"
            :url          "http://www.eclipse.org/legal/epl-v10.html"}

  :aliases {"mkdocs" ["art"]}

  :art {:templates  ["assets/README.md.art"]
        :output-dir "."}

  :dependencies [[org.clojure/tools.cli "0.4.2"]
                 [vivid/ash-ra-template "0.4.0"]]

  :dev-dependencies [[org.clojure/clojure "1.9.0"]
                     [leiningen "2.9.1"]]

  :eval-in-leiningen true

  :exclusions [org.clojure/clojure]

  :global-vars {*warn-on-reflection* true}

  :javac-options ["-target" "1.8"]

  :manifest {"Built-By" "vivid"}

  :min-lein-version "2.9.1"

  :profiles {:dev {:dependencies [;; Diffs equality assertions in test failure output
                                  ;; https://github.com/pjstadig/humane-test-output
                                  [pjstadig/humane-test-output "0.9.0"]]

                   :injections   [(require 'pjstadig.humane-test-output)
                                  (pjstadig.humane-test-output/activate!)]

                   :plugins      [;; Reloads & re-runs tests on file changes
                                  ;; https://github.com/jakemcc/lein-test-refresh
                                  [com.jakemccrary/lein-test-refresh "0.24.1"]]

                   :resource-paths ["test-resources"]

                   :test-refresh {:quiet true}}}

  :repositories [["clojars" {:sign-releases false}]])
