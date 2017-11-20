(defproject ash-ra-template "0.1.0"

  :description "Ash Ra Template: Minimal template library for Clojure featuring Ruby 2.0 ERB syntax and Clojure language processing."
  :url "https://github.com/vivid/ash-ra-template"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [eval-soup                   "1.2.3"]
                 [pjstadig/humane-test-output "0.8.3"]
                 [reduce-fsm                  "0.1.4"]]

  :injections [(require 'pjstadig.humane-test-output)
               (pjstadig.humane-test-output/activate!)]

  :javac-options ["-target" "1.8"]

  :profiles {:dev {:global-vars {*warn-on-reflection* true}
                   :plugins [[com.jakemccrary/lein-test-refresh "0.21.1"]]}}

  :test-refresh {:quiet true})
