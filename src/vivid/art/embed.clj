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

(defn- resolve-deps
  ([] (resolve-deps {}))
  ([deps]
   (deps/resolve-deps
     {:deps      (merge DEFAULT_DEPS deps)
      :mvn/repos DEFAULT_REPOS}
     nil)))

(defn- build-classpath [deps]
  (deps/make-classpath deps nil nil))

(defn- classpath-segments [classpath]
  (string/split classpath (Pattern/compile (Pattern/quote File/pathSeparator))))

(defn- new-rt-shim [^ClassLoader classloader]
  (doto (.newInstance (.loadClass classloader RUNTIME_SHIM_CLASS))
    (.setClassLoader classloader)
    (.setName (name (gensym "vivid-art-embed-runtime")))
    (.init)))

(defn- construct-class-loader [classes]
  (let [it (JarClassLoader.)]
    (doseq [clazz classes] (.add it clazz))
    (.setEnabled (.getParentLoader it) false)
    (.setEnabled (.getSystemLoader it) false)
    (.setEnabled (.getThreadLoader it) false)
    (.setEnabled (.getOsgiBootLoader it) false)
    it))

(defn- unload-classes-from-loader [^JarClassLoader loader]
  (let [loaded (doall (keys (.getLoadedClasses loader)))]
    (doseq [clazz loaded] (.unloadClass loader clazz))))

; ---- Public API ----

(defn close-runtime! [runtime]
  (.close runtime)
  (unload-classes-from-loader
    (.getClassLoader runtime)))

(defn eval-in-runtime [runtime code-as-string]
  (letfn [(call [fqsym code] (.invoke runtime fqsym code))]
    (call "clojure.core/load-string" code-as-string)))

(defmacro with-runtime [runtime & body]
  (let [text (pr-str (conj body 'do))]
    `(eval-in-runtime ~runtime ~text)))

(defn new-runtime
  ([] (new-runtime {}))
  ([deps]
   (->> deps
        (resolve-deps)
        (build-classpath)
        (classpath-segments)
        (construct-class-loader)
        (new-rt-shim))))

(defn eval-in-one-shot-runtime
  [s]
  (let [rt (new-runtime)]
    (try (eval-in-runtime rt s)
         (finally (close-runtime! rt)))))

(defmacro with-one-shot-runtime [& body]
  `(let [runtime# (new-runtime)]
     (try (with-runtime runtime# ~@body)
          (finally (close-runtime! runtime#)))))
