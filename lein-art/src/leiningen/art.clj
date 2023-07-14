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

(ns leiningen.art
  (:require
   [clojure.string]
   [clojure.tools.cli]
   [farolero.core :as farolero]
   [leiningen.core.classpath]
   [leiningen.core.main :as main-lein]
   [vivid.art.cli.args]
   [vivid.art.cli.exec]
   [vivid.art.cli.log :as log]
   [vivid.art.cli.messages :as messages]
   [vivid.art.cli.usage]))

(def ^:const default-options {:output-dir "."})

(defn- exit [exit-status message]
  (main-lein/info message)
  (main-lein/exit exit-status))

(defn- batch-from-cli-args [args]
  (let [batch* (vivid.art.cli.args/cli-args->batch args vivid.art.cli.usage/cli-options)
        batch (merge default-options batch*)]
    batch))

(defn- batches-from-project
  [project]
  (let [stanza        (:art project)
        prj-classpath (leiningen.core.classpath/get-classpath project)
        ->batch       #(->> (vivid.art.cli.args/direct->batch (:templates %) %)
                            (merge default-options
                                   {:classpath prj-classpath}))]
    (cond
      (map? stanza)  [(->batch stanza)]
      (coll? stanza) (map ->batch stanza)
      :else (exit 1 "Error: Uninterpretable lein-art ART configuration"))))

(defn- process [project command args]
  (binding [log/*debug-fn* main-lein/debug
            log/*info-fn*  main-lein/info
            log/*warn-fn*  main-lein/warn]
    ; TODO Documentation: Clarify that specifying options will cause ART to ignore project settings.
    (let [batches (if (coll? args)
                    [(batch-from-cli-args args)]
                    (batches-from-project project))]
      (vivid.art.cli.exec/dispatch-command command batches))))

(defn- usage []
  (let [options-summary (:summary (clojure.tools.cli/parse-opts [] vivid.art.cli.usage/cli-options))]
    (->> [vivid.art.cli.usage/one-line-desc
          (vivid.art.cli.usage/summary "Leiningen plugin")
          "Usage: lein art command [options...] [template-files...]"
          (str "Commands:\n" (vivid.art.cli.usage/command-summary))
          (str "Options:\n" options-summary)
          vivid.art.cli.usage/for-more-info]
         (clojure.string/join "\n\n"))))

;
; Leiningen entry point for lein-art
;

(defn ^:no-project-needed
  ^{:doc (usage)}
  art [project command & args]
  (farolero/handler-case
    (process project command args)
    (:vivid.art.cli/error [_ details] (if (:show-usage details)
                                        (exit (or (:exit-status details) 1) (usage))
                                        (main-lein/abort (messages/pp-str-error details))))))
