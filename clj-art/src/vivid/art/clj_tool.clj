; Copyright 2020 Vivid Inc.
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;    https://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns vivid.art.clj-tool
  (:require
    [clojure.string]
    [clojure.tools.cli]
    [special.core :as special]
    [vivid.art.cli.args]
    [vivid.art.cli.exec]
    [vivid.art.cli.log :as log]
    [vivid.art.cli.usage :refer [cli-options]]))

(defn- exit [exit-status message]
  (println message)
  (System/exit exit-status))

(defn- from-cli-args [args]
  (-> (vivid.art.cli.args/cli-args->batch args cli-options)
      (vivid.art.cli.exec/render-batch)))

(defn- process [args]
  (binding [log/*info-fn* println
            log/*warn-fn* println]
    (from-cli-args args)))

(defn- usage []
  (let [options-summary (:summary (clojure.tools.cli/parse-opts [] cli-options))]
    (->> [(vivid.art.cli.usage/summary "Clojure tool")
          (str "Usage: clj -m " (namespace `usage) " [options...] template-files...")
          (str "Options:\n" options-summary)
          "A rendering batch can also be specified as an alias in `deps.edn':"
          "  {:aliases {:art {:extra-deps {vivid/clj-art {:mvn/version \"0.5.0\"}}
                     :main-opts [\"-m\" \"vivid.art.clj-tool\"
                                 options... template-files...]}}}"
          "Options are supplied identically to the CLI invocation. Run the ART alias with:"
          "  $ clojure -A:art"
          (vivid.art.cli.usage/finer-details "as a Clojure tool alias")
          vivid.art.cli.usage/for-more-info]
         (clojure.string/join "\n\n"))))

(defn -main
  "Clojure tools entry point for clj-art."
  [& args]
  ((special/manage process
                   :vivid.art.cli/error #(if (:show-usage %)
                                           (exit (or (:exit-status %) 1) (usage))
                                           (exit 1 (str "ART error: " (:message %)))))
   args))
