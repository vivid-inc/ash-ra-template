; Copyright 2019 Vivid Inc.

(ns vivid.art.leiningen.resolve
  (:import
    (java.io IOException PushbackReader))
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.spec.alpha :as s]))

(defn resolve-as-edn-file
  "Load EDN from an io/reader source (filename or io/resource)."
  [source]
  (try
    (with-open [r (io/reader (io/file (System/getProperty "user.dir") source))]
      (edn/read (PushbackReader. r)))
    (catch IOException _
      #_(main-lein/warn (format "Couldn't open '%s': %s\n" source (.getMessage e))))
    (catch RuntimeException _
      #_(main-lein/warn (format "Error parsing EDN from '%s': %s\n" source (.getMessage e))))))

(defn resolve-as-edn-literal
  "Attempts to interpret a string as EDN."
  [^String s]
  (if s
    (try
      (edn/read-string s)
      (catch Throwable _))))

(defn resolve-as-map
  "Attempts to interpret a value as a Clojure map."
  [v]
  (when (map? v)
    v))

(defn resolve-as-var
  "Attempts to interpret a string as the name of a namespace-qualified var,
  defaulting to default-ns if not specified."
  [^String s & [default-ns]]
  (if s
    (when-let [v (if default-ns
                   (ns-resolve default-ns (symbol s))
                   (resolve (symbol s)))]
      (var-get v))))

(defn resolve-bindings
  [bindings]
  (reduce
    (fn [bs b]
      (let [r (or (resolve-as-map b)                        ; TODO write a test for this
                  (resolve-as-var b)
                  (resolve-as-edn-file b)
                  (resolve-as-edn-literal b))]
        (if (map? r)
          (concat bs [r])
          bs)))
    [] bindings))

(defn resolve-delimiters
  [^String s]
  (as-> s d
        (or (resolve-as-var d 'vivid.art.delimiters)
            (resolve-as-edn-literal d))
        (s/conform :vivid.art/delimiters d)
        (when-not (s/invalid? d) d)))                       ; TODO Raise condition otherwise
