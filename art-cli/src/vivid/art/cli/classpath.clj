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

(ns vivid.art.cli.classpath
  (:require
   [cemerick.pomegranate :as pomegranate]
   [cemerick.pomegranate.aether :as aether]
   [clojure.set]
   [vivid.art.cli.log :as log])
  (:import
   (clojure.lang DynamicClassLoader)
   (java.io File)
   (java.net URI)))

(defn dependencies->file-paths
  "Resolves a Lein-style list of dependencies, returning their file paths."
  [dependencies]
  (->> (aether/resolve-dependencies :coordinates dependencies
                                    :repositories (merge aether/maven-central
                                                         {"clojars" "https://clojars.org/repo"}))
       (aether/dependency-files)
       (map #(.getAbsolutePath ^File %))))

(defn strip-uri-scheme
  "paths is a seq of java.lang.String formatted like a URI i.e.
          file:/home/me/.m2/repository/.../bleep.jar
      Returns a seq of strings without the scheme, file or any other."
  [paths]
  (map (fn [path]
         (let [uri (URI. path)]
           (when (not= "file" (.getScheme uri))
             (log/*warn-fn* "Path " uri " scheme is not 'file:'; proceeding blindly."))
           (.getPath uri)))
       paths))

(defmacro with-custom-classloader [classpath & body]
  `(let [thread#    (Thread/currentThread)
         cl#        (.getContextClassLoader thread#)
         dcl#       (DynamicClassLoader. cl#)
         cur-ps#    (into #{} (strip-uri-scheme (pomegranate/get-classpath)))
         prj-ps#    (into #{} ~classpath)
         delta-ps#  (clojure.set/difference prj-ps# cur-ps#)]
     (try (.setContextClassLoader thread# dcl#)
          (doseq [path# delta-ps#]
            (pomegranate/add-classpath (.getPath (URI. path#))))
          ~@body
          (finally
                       ; TODO The initial classloader cl# is still encumbered with the paths added from delta#, defeating the purpose of this mechanism.
            (.setContextClassLoader thread# cl#)
            (.close dcl#)))))
