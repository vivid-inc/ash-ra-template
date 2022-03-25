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
   [cemerick.pomegranate :as pomegranate]
   [clojure.string]
   [clojure.tools.cli]
   [farolero.core :as farolero]
   [leiningen.core.classpath :as classpath]
   [leiningen.core.main :as main-lein]
   [vivid.art.cli.args]
   [vivid.art.cli.exec]
   [vivid.art.cli.log :as log]
   [vivid.art.cli.messages :as messages]
   [vivid.art.cli.usage])
  (:import
   (clojure.lang DynamicClassLoader)
   (java.net URI)))

(def ^:const default-options {:output-dir "."})

(defn- exit [exit-status message]
  (main-lein/info message)
  (main-lein/exit exit-status))

(defn- from-cli-args [args]
  (let [batch* (vivid.art.cli.args/cli-args->batch args vivid.art.cli.usage/cli-options)
        batch (merge default-options batch*)]
    (vivid.art.cli.exec/render-batch batch)))

(defn- from-project
  [project]
  (let [stanza (:art project)
        pipeline #(->> (vivid.art.cli.args/direct->batch (:templates %) %)
                       (merge default-options)
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
  (let [options-summary (:summary (clojure.tools.cli/parse-opts [] vivid.art.cli.usage/cli-options))]
    (->> [vivid.art.cli.usage/one-line-desc
          (vivid.art.cli.usage/summary "Leiningen plugin")
          "Usage: lein art [options...] template-files..."
          (str "Options:\n" options-summary)
          vivid.art.cli.usage/for-more-info]
         (clojure.string/join "\n\n"))))

;
; Classpath setup
;

(defn strip-uri-file-scheme [paths]
  ; paths is a seq of java.lang.String formatted like a URI i.e. "file:/home/me/.m2/repository/.../bleep.jar"
  (map (fn [path]
         (let [uri (URI. path)]
           (when (not= "file" (.getScheme uri))
             (log/*warn-fn* "ART WARNING: Path " uri " scheme is not 'file:'; proceeding blindly."))
           (.getPath uri)))
       paths))

; TODO Migrate this mechanism to art-cli, replacing the ShimDandy -based :dependencies, and add equivalent mechanisms to boot-art and clj-art
(defmacro with-custom-classloader [project & body]
  `(let [thread#    (Thread/currentThread)
         cl#        (.getContextClassLoader thread#)
         dcl#       (DynamicClassLoader. cl#)
         cur-ps#    (into #{} (strip-uri-file-scheme (pomegranate/get-classpath)))
         prj-ps#    (into #{} (classpath/get-classpath ~project))
         delta-ps#  (clojure.set/difference prj-ps# cur-ps#)]
     (try (.setContextClassLoader thread# dcl#)
          (doseq [path# delta-ps#]
            (pomegranate/add-classpath (.getPath (URI. path#))))
          ~@body
          (finally
            ; TODO The initial classloader cl# is still encumbered with the paths added from delta#, defeating the purpose of this mechanism.
            (.setContextClassLoader thread# cl#)
            (.close dcl#)))))

;
; Leiningen entry point for lein-art
;

(defn ^:no-project-needed
  ^{:doc (usage)}
  art [project & args]
  (with-custom-classloader
    project
    (farolero/handler-case
     (process project args)
     (:vivid.art.cli/error [_ details] (if (:show-usage details)
                                         (exit (or (:exit-status details) 1) (usage))
                                         (main-lein/abort (messages/pp-str-error details)))))))
