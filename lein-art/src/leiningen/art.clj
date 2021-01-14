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

(ns leiningen.art
  (:require
    [clojure.string]
    [clojure.tools.cli]
    [leiningen.core.main :as main-lein]
    [special.core :as special]
    [vivid.art.cli.args]
    [vivid.art.cli.exec]
    [vivid.art.cli.log :as log]
    [vivid.art.cli.usage]))

(defn- exit [exit-status message]
  (main-lein/info message)
  (main-lein/exit exit-status))

(defn- from-cli-args [args]
  (let [batch (vivid.art.cli.args/cli-args->batch args vivid.art.cli.usage/cli-options)]
    (vivid.art.cli.exec/render-batch batch)))

(defn- from-project
  [project]
  (let [stanza (:art project)
        pipeline #(-> (vivid.art.cli.args/direct->batch (:templates %) %)
                      (vivid.art.cli.exec/render-batch))]
    (cond
      (map? stanza) (pipeline stanza)
      (coll? stanza) (doseq [conf stanza]
                       (pipeline conf))
      :else (main-lein/warn "Warning: Unknown lein-art ART configuration"))))

(defn- process [project args]
  (binding [log/*info-fn* main-lein/info
            log/*warn-fn* main-lein/warn]
    (if (coll? args)
      (from-cli-args args)
      (from-project project))))

(defn- usage []
  ; TODO Unify this with assets/README.md
  (let [options-summary (:summary (clojure.tools.cli/parse-opts [] vivid.art.cli.usage/cli-options))]
    (->> [vivid.art.cli.usage/one-line-desc
          (vivid.art.cli.usage/summary "Leiningen plugin")
          "Usage: lein art [options...] template-files..."
          (str "Options:\n" options-summary)
          vivid.art.cli.usage/for-more-info]
         (clojure.string/join "\n\n"))))

; Leiningen entry point for lein-art
(defn ^:no-project-needed
  ^{:doc (usage)}
  art [project & args]
  ((special/manage process
                   :vivid.art.cli/error #(if (:show-usage %)
                                           (exit (or (:exit-status %) 1) (usage))
                                           (main-lein/abort (str "ART error: " (:message %)))))
   project args))
