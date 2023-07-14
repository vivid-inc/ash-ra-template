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

(ns vivid.art.clj-tool
  (:require
   [clojure.string]
   [clojure.tools.cli]
   [farolero.core :as farolero]
   [vivid.art.cli.args]
   [vivid.art.cli.exec]
   [vivid.art.cli.log :as log]
   [vivid.art.cli.messages :as messages]
   [vivid.art.cli.usage :refer [cli-options]]))

(def ^:const default-options {:output-dir "."})

(defn- exit [exit-status message]
  (println message)
  ; TODO Clojure doesn't exit right away. https://clojureverse.org/t/why-doesnt-my-program-exit/3754
  (System/exit exit-status))

(defn- from-cli-args [args]
  (->> (vivid.art.cli.args/cli-args->batch args cli-options)
       (merge default-options)
       (vivid.art.cli.exec/render-batch)))

(defn- process [args]
  (binding [log/*info-fn* println
            log/*warn-fn* println]
    (from-cli-args args)))

(defn usage []
  (let [options-summary (:summary (clojure.tools.cli/parse-opts [] cli-options))]
    (->> [vivid.art.cli.usage/one-line-desc
          (vivid.art.cli.usage/summary "Clojure tool")
          (str "Usage: clj -m " (namespace `usage) " [options...] template-files...")
          (str "Options:\n" options-summary)
          vivid.art.cli.usage/for-more-info]
         (clojure.string/join "\n\n"))))

(defn -main
  "Clojure tools entry point for clj-art."
  [& args]
  ; TODO Include classpath and deps
  (farolero/handler-case (process args)
                         (:vivid.art.cli/error [_ details] (if (:show-usage details)
                                                             (exit (or (:exit-status details) 1) (usage))
                                                             (exit 1 (messages/pp-str-error details))))))
