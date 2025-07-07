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

; See https://clojure.org/guides/deps_and_cli

(ns ^:internal-api vivid.art.clj-tool
  (:require
   [clojure.edn]
   [clojure.java.io :as io]
   [clojure.string]
   [clojure.tools.cli]
   [farolero.core :as farolero]
   [vivid.art.cli :refer [default-options]]
   [vivid.art.cli.args]
   [vivid.art.cli.command]
   [vivid.art.cli.log :as log]
   [vivid.art.cli.messages :as messages]
   [vivid.art.cli.usage :as usage])
  (:import
   (java.io PushbackReader)))

(set! *warn-on-reflection* true)

(defn- exit [exit-status message]
  (println message)
  ; TODO Clojure doesn't exit right away. https://clojureverse.org/t/why-doesnt-my-program-exit/3754
  ; TODO The main point isn't to exit, but to return a quasi 'exit code' back to our caller.
  (shutdown-agents)
  (System/exit exit-status))

(defn- batches-from-project
  [project]
  (let [stanza  (:art project)
        ->batch #(->> (vivid.art.cli.args/direct->batch (:templates %) %)
                      (merge default-options))]
    (cond
      (map? stanza) [(->batch stanza)]
      (coll? stanza) (map ->batch stanza)
      :else (exit 1 "Error: Uninterpretable clj-art ART configuration"))))

(defn- process [project command args]
  (binding [log/*info-fn* println
            log/*warn-fn* println]
    ; TODO Documentation: Clarify that specifying options will cause ART to ignore project settings.
           (let [batches (if (coll? args)
                           [(vivid.art.cli/batch-from-cli-args args)]
                           (batches-from-project project))]
                (vivid.art.cli.command/dispatch-command command batches)
                (shutdown-agents))))

(defn usage []
  (let [options-summary (:summary (clojure.tools.cli/parse-opts [] usage/cli-options))]
    (->> [usage/one-line-desc
          (usage/summary "Clojure tool")
          (str "Usage: clj -m " (namespace `usage) " COMMAND [OPTION]... [TEMPLATE-FILE]...")
          (str "Commands:\n" (usage/command-summary))
          (str "Options:\n" options-summary)
          usage/for-more-info]
         (clojure.string/join "\n\n"))))

(defn read-edn-file
  [path]
  (with-open [stream (PushbackReader. (io/reader path))]
    (clojure.edn/read stream)))

;
; Clojure tools entry point for clj-art
;

(defn -main
  "Clojure tools entry point for clj-art."
  ; Classpath is already set by Clojure deps tool.
  [& args]
  (let [project  (read-edn-file "deps.edn")
        has-cmd? (not (.startsWith (or ^String (first args) "") "-"))
        command  (when has-cmd? (first args))
        args*    (if has-cmd? (seq (rest args)) args)]
    (farolero/handler-case
     (process project command args*)
     (:vivid.art.cli/error [_ details] (if (:show-usage details)
                                         (exit (or (:exit-status details) 1) (usage))
                                         (exit 1 (messages/pp-str-error details)))))))
