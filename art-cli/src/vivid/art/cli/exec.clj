; Copyright 2022 Vivid Inc. and/or its affiliates.
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

(ns vivid.art.cli.exec
    "Non-lazily drives the rendering of batches."
    (:require
    [clojure.java.io :as io]
    [clojure.pprint]
    [clojure.spec.alpha :as s]
    [clojure.string]
    [farolero.core :as farolero]
    [vivid.art :as art]
    [vivid.art.cli.files :as files]
    [vivid.art.cli.classpath :refer [with-custom-classloader]]
    [vivid.art.cli.log :as log]
    [vivid.art.cli.messages :as messages]
    [vivid.art.cli.specs]
    [vivid.art.cli.watch])
    (:import
      (java.io File)
      (java.nio.file Path)))

(defn- render-file
  [{:keys [^File src-path ^File dest-rel-path] :as template-file} {:keys [^File output-dir] :as batch}]
  (try
    (let [output-path ^File (io/file output-dir dest-rel-path)
          to-phase (get batch :to-phase vivid.art/default-to-phase)]
      (log/*info-fn* (format "Rendering ART %s" (.getCanonicalFile output-path)))
      (io/make-parents output-path)
      (as-> (slurp src-path) c
            (apply art/render c (mapcat identity
                                        (select-keys batch [:bindings
                                                            :delimiters
                                                            :dependencies
                                                            :to-phase])))
            (if (to-phase #{:parse :translate})
              (clojure.pprint/pprint c (io/writer output-path)) ; Possibly more readable
              (spit output-path c))))
    (catch Exception e
      (farolero/signal :vivid.art.cli/error
                       {:step      'render-file
                        :message   (format "Exception while rendering ART template %s"
                                           (.getCanonicalPath ^File (:src-path template-file)))
                        :exception e}))))

(defn assemble-classpath
      [batch]
      ; TODO Derive repositories from the calling project as well, provide a default set (Maven Central + Clojars)
      ; TODO Documentation: Add differing versions of libraries that are already loaded at your own peril.
      (concat []
              (:classpath batch)
              (vivid.art.cli.classpath/dependencies->file-paths (:dependencies batch))))

(defn render-batch
  "Scans :templates for files and directory sub-trees, renders all ART templates found
  within according to the batch settings. Fails fast in event of an error."
  [batch]
  (let [templates (-> (:templates batch)
                      files/paths->template-paths!)]
       (if (empty? templates)
         (log/*warn-fn* "Warning: No ART templates to render.")
         (let [classpath (assemble-classpath batch)]
              (with-custom-classloader classpath
                                       (doseq [template-file templates]
                                              (render-file template-file batch)))))))

(s/fdef render-batch
        :args (s/cat :batch (s/? :vivid.art.cli/batch)))

(defn render-batches-once [batches]
      (doseq [b batches]
             (render-batch b)))

(defn parent? [^File parent ^File file]
  (when (.startsWith (.toPath file) (.toPath parent))
    parent))

(defn first-matching-parent [parents ^File file]
  (some #(parent? % file) parents))

; TODO De-bounce / coalesce writes, on each input file.
; https://stackoverflow.com/questions/35663415/throttle-functions-with-core-async
; https://ericnormand.me/guide/clojure-concurrency
#_(defn debounce [file]
  (let [out (chan)]
    (go-loop [last-val nil]
             (let [val (if (nil? last-val) (<! in) last-val)
                   timer (timeout 50)
                   [new-val ch] (alts! [in timer])]
               (condp = ch
                 timer (do (>! out val) (recur nil))
                 in    (recur new-val))))
    out))

(defn render-from-watch-event [batch {:keys [path type] :as watch-event}]
  (let [file (.toFile ^Path path)]
    (when (vivid.art.cli.files/art-template-file? file)
      (cond
        (get #{:create :modify} type)
        ; Re-use (render-batch) but pass in only paths of the affected template file.
        (let [base-path (first-matching-parent (:templates batch) file)
              tp        (files/->template-path base-path file)
              of        (File. ^File (:output-dir batch)
                               ^String (.getParent (:dest-rel-path tp)))
              batch*    (assoc batch :templates [file] :output-dir of)]
          (render-batch batch*))

        :else
        (log/*debug-fn* "Ignoring beholder event:" (pr-str watch-event))))))

(defn dispatch-command [command batches]
      (condp = command

             "auto"
             (do
               (render-batches-once batches)
               (vivid.art.cli.watch/watch-on-batches batches render-from-watch-event))

             "config"
             (clojure.pprint/pprint batches)

             "help"
             (farolero/signal :vivid.art.cli/error
                              {:step        'parse-cli-args
                               :exit-status 0
                               :show-usage  true})

             "render"
             (render-batches-once batches)

             (farolero/signal :vivid.art.cli/error
                              {:step    'parse-cli-args
                               :message (messages/pp-str-error (str "Unknown command: `" command "'"))})))

