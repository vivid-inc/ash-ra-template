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

(ns ^:internal-api vivid.art.cli.command
  "CLI command parsing and dispatch."
  (:require
   [farolero.core :as farolero]
   [vivid.art.cli :as art-cli]
   [vivid.art.cli.files :as files]
   [vivid.art.cli.log :as log]
   [vivid.art.cli.messages :as messages]
   [vivid.art.cli.watch])
  (:import
   (java.io File)
   (java.nio.file Path)))

(defn parent? [^File parent ^File file]
  (when (.startsWith (.toPath file) (.toPath parent))
    parent))

(defn first-matching-parent [parents ^File file]
  (some #(parent? % file) parents))

(defn render-from-watch-event [batch {:keys [path type] :as watch-event}]
  (let [file (.toFile ^Path path)]
    (when (files/art-template-file? file)
      (cond
        (get #{:create :modify} type)
        ; Re-use (render-batch) but pass in only paths of the affected template file.
        (let [base-path (first-matching-parent (:templates batch) file)
              tp        (files/->template-path base-path file)
              of        (File. ^File (:output-dir batch)
                               ^String (.getParent ^File (:dest-rel-path tp)))
              batch*    (assoc batch :templates [file] :output-dir of)]
          (art-cli/render-batch batch*))

        :else
        (log/*debug-fn* "Ignoring beholder event:" (pr-str watch-event))))))

(defn dispatch-command [command batches]
  (condp = command

    "config"
    (clojure.pprint/pprint batches)

    "help"
    (farolero/signal :vivid.art.cli/error
                     {:step        'parse-cli-args
                      :exit-status 0
                      :show-usage  true})

    "render"
    (art-cli/render-batches batches)

    "watch"
    (do
      (log/*info-fn* "Press CTRL-C to interrupt watch")
      (art-cli/render-batches batches)
      (vivid.art.cli.watch/watch-on-batches batches render-from-watch-event))

    (farolero/signal :vivid.art.cli/error
                     {:step    'parse-cli-args
                      :message (messages/pp-str-error (str "Unknown command: `" command "'"))})))
