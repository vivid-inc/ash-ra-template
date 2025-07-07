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

(ns ^:internal-api vivid.art.cli.watch
  (:require
   [vivid.art.cli.debounce :as debounce]
   [vivid.art.cli.log :as log])
  (:import
   (io.methvin.watcher DirectoryChangeEvent DirectoryChangeListener DirectoryWatcher)
   (java.io File)
   (java.nio.file Files NoSuchFileException Path)))

; Implementation notes:
;
; Employ io.methvin:directory-watcher to watch for file system changes. It is
; fully recursive; it will report events occurring on all sub-paths of the
; supplied directories.
;
; Attempt to preserve the order in which batches and templates are declared and
; processed so that users can rely on their spatial memory when parsing tool
; output.
;
; File system modifications for a particular batch might occur in clumps and
; flurries. Immediate embarkation on a re-render of such a batch might be
; premature if it occurred before file system activity died back down.
; Debouncing re-render requests will strike a balance between:
; - Avoiding wasted intermediate re-renders that expend computational resources
;   while delaying time to last re-render.
; - ART's apparent responsiveness to changes.

(def ^:const default-debounce-timeout-ms
  "io.methvin:directory-watcher:0.17.3 uses a value of 500ms wait period
  in its automated test suite; we adopt that value here."
  500)

(def ^:const batch-defaults
  {:watch-timeout-ms default-debounce-timeout-ms})

;
; directory-watcher
;

(def ^:const noise-exception-types
  #{; IDEs may save file edits to a temporary peer file before swapping it
    ; into place.
    NoSuchFileException})

(defn listener ^DirectoryChangeListener
  [listener-fn]
  (reify
    DirectoryChangeListener
    (^void onEvent [_ ^DirectoryChangeEvent event]
      (listener-fn {:path (.path event)
                    :type (.name (.eventType event))}))
    (onException [_ e]
     ; De-noise exception output
      (when-not (noise-exception-types (type e))
        (log/*warn-fn* e)))))

(defn ^DirectoryWatcher build-directory-watcher
  [listener-fn path]
  (-> (DirectoryWatcher/builder)
      (.path path)
      (.listener (listener listener-fn))
      (.build)))

(defn watch
  "Asynchronously watches for file system changes on the specified path,
  calling listener-fn on each detected change. This function returns
  immediately."
  [listener-fn ^Path path]
  (doto ^DirectoryWatcher (build-directory-watcher listener-fn path)
    (.watchAsync)))

;
; Handler fns
;

(defn directory-handler-fn
  [batch ^File f]
  (fn [event-fn]
    (let [path-str     (.toString f)
          debounced-fn (fn [_] (debounce/debounce (:watch-timeout-ms batch) event-fn batch))]
      (log/*info-fn* "Watching" path-str)
      (watch debounced-fn (.toPath f)))))

(defn file-handler-fn
  "io.methvin:directory-watcher only handles directories.
  We work around this limitation by watching the file's parent directory,
  ignoring all events except those that match the file path."
  [batch ^File f]
  (fn [event-fn]
    (let [parent-dir (.getParentFile f)
          path-str   (.toString f)
          my-event-fn (fn [event]
                        (when (Files/isSameFile (.toPath f) (:path event))
                          (debounce/debounce (:watch-timeout-ms batch) event-fn batch)))]
      (log/*info-fn* "Watching" path-str)
      (watch my-event-fn (.toPath parent-dir)))))

(defn not-exists-handler-fn
  [_ ^File f]
  (fn [_]
    (log/*warn-fn* "Ignoring non-existent template path:" (.toString f))))

;
; art
;

(defn batch->handlers
  "Lists handlers for each of the template paths in a batch."
  [acc batch']
  (let [batch (merge batch-defaults
                     batch')]
    (reduce (fn [acc' template-info]
              (let [path (:src-path template-info)
                    handler (cond
                              (= (:oriented-as template-info) :directory) (directory-handler-fn batch path)
                              (= (:oriented-as template-info) :file) (file-handler-fn batch path)
                              :else (not-exists-handler-fn batch path))]
                (concat acc' [handler])))
            acc
            (:templates batch))))

(defn watch-and-sleep!
  [handlers event-fn]
  (doseq [h handlers]
    (h event-fn))
  (debounce/debounce-go-loop)
  (while true
    (Thread/sleep Long/MAX_VALUE)))

(defn watch-on-batches
  "Watches template paths in all supplied batches.
  Whenever a file system event occurs associated with a batch, calls
  event-fn with batch."
  [batches event-fn]
  (let [handlers (reduce batch->handlers [] batches)]
    (if (seq handlers)
      (watch-and-sleep! handlers event-fn)
      (log/*warn-fn* "No template paths to watch; exiting"))))
