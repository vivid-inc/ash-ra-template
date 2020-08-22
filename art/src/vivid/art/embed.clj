; Attribution: Based on clj-embed

; Portions Copyright 2019 Vivid Inc.
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

(ns vivid.art.embed
  (:require
    [clojure.string :as string]
    [clojure.tools.deps.alpha :as deps])
  (:import
    (java.io File)
    (java.util.regex Pattern)
    (org.xeustechnologies.jcl JarClassLoader)))

(def ^:const DEFAULT_REPOS
  {"central" {:url "https://repo1.maven.org/maven2/"}
   "clojars" {:url "https://clojars.org/repo/"}})

(def ^:const DEFAULT_DEPS
  {'org.clojure/clojure                     {:mvn/version "1.9.0"}
   'org.projectodd.shimdandy/shimdandy-api  {:mvn/version "1.2.1"}
   'org.projectodd.shimdandy/shimdandy-impl {:mvn/version "1.2.1"}})

(def ^:const RUNTIME_SHIM_CLASS
  "org.projectodd.shimdandy.impl.ClojureRuntimeShimImpl")

(defn- build-classpath [deps]
  (deps/make-classpath deps [] {}))

(defn- classpath-segments [classpath]
  (string/split classpath (Pattern/compile (Pattern/quote File/pathSeparator))))

(defn- construct-class-loader ^JarClassLoader [classes]
  (let [it (JarClassLoader.)]
    (doseq [clazz classes]
      (.add it clazz))
    (.setEnabled (.getParentLoader it) false)
    (.setEnabled (.getSystemLoader it) false)
    (.setEnabled (.getThreadLoader it) false)
    (.setEnabled (.getOsgiBootLoader it) false)
    it))

(defn- new-rt-shim [^JarClassLoader classloader]
  (doto (.newInstance (.getDeclaredConstructor (.loadClass classloader RUNTIME_SHIM_CLASS)
                                               (into-array Class []))
                      (into-array Object []))
    (.setClassLoader classloader)
    (.setName (name (gensym "vivid-art-runtime")))
    (.init)))

(defn- resolve-deps [deps]
  (let [d {:deps      (merge DEFAULT_DEPS deps)
           :mvn/repos DEFAULT_REPOS}]
    (deps/resolve-deps d {})))

(defn- unload-classes-from-loader [^JarClassLoader loader]
  (let [loaded (doall (keys (.getLoadedClasses loader)))]
    (doseq [clazz loaded]
      (.unloadClass loader clazz))))

(defn close-runtime! [runtime]
  (.close runtime)
  (unload-classes-from-loader
    (.getClassLoader runtime)))

(defn eval-in-runtime [runtime code-as-string]
  (letfn [(call [fqsym code] (.invoke runtime fqsym code))]
    (call "clojure.core/load-string" code-as-string)))

(defn new-runtime
  [deps]
  (->> deps
       (resolve-deps)
       (build-classpath)
       (classpath-segments)
       (construct-class-loader)
       (new-rt-shim)))

(defn eval-in-one-shot-runtime
  [s deps]
  (let [rt (new-runtime deps)]
    (try (eval-in-runtime rt s)
         (finally (close-runtime! rt)))))
