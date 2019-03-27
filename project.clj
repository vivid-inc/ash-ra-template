(defproject vivid/ash-ra-template "0.2.0"

  :description "Ash Ra Template: Minimal template library for Clojure featuring Ruby 2.0 ERB syntax and Clojure language processing."
  :url "https://github.com/vivid/ash-ra-template"
  :license {:distribution :repo
            :name         "Eclipse Public License"
            :url          "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [eval-soup "1.5.0"]
                 [pjstadig/humane-test-output "0.9.0"]
                 [reduce-fsm "0.1.4"]]

  :global-vars {*warn-on-reflection* true}

  :injections [(require 'pjstadig.humane-test-output)
               (pjstadig.humane-test-output/activate!)]

  :javac-options ["-target" "1.8"]

  :plugins [[lein-ancient "0.6.15"]
            [jonase/eastwood "0.3.5"]]

  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh "0.24.0"]]}}

  :repositories [["clojars" {:sign-releases false}]]

  :test-refresh {:quiet true})
