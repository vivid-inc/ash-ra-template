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

(ns ^:internal-api vivid.art.cli.command
  "CLI command parsing and dispatch."
  (:require
   [clojure.string]
   [farolero.core :as farolero]
   [vivid.art.cli :as art-cli]
   [vivid.art.cli.log :as log]
   [vivid.art.cli.messages :as messages]
   [vivid.art.cli.watch]))

(defn dispatch-command [command' batches]
  (let [command (-> (or command' "")
                    (clojure.string/lower-case)
                    (clojure.string/trim))]
    (cond

      (= command "config")
      (clojure.pprint/pprint batches)

      (get #{nil "" "help"} command)
      (farolero/signal :vivid.art.cli/error
                       {:step        'parse-cli-args
                        :exit-status 0
                        :show-usage  true})

      (= command "render")
      (art-cli/render-batches batches)

      (= command "watch")
      (do
        (log/*info-fn* "Press CTRL-C to interrupt watch")
        (art-cli/render-batches batches)
        (vivid.art.cli.watch/watch-on-batches batches art-cli/render-batch))

      (= command "version")
      (log/*info-fn* vivid.art.cli.usage/one-line-desc)

      :else
      (farolero/signal :vivid.art.cli/error
                       {:step    'parse-cli-args
                        :message (messages/pp-str-error (str "Unknown command: `" command' "'"))
                        :command command'}))))
