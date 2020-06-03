; Copyright 2019 Vivid Inc.

(ns vivid.art.leiningen.cli
  (:require
    [clojure.string :as str]
    [clojure.tools.cli]
    [leiningen.core.main :as main-lein]
    [leiningen.core.project]
    [vivid.art]
    [vivid.art.leiningen.cli-options :refer [cli-options]]
    [vivid.art.leiningen.resolve :as rslv]))

(def ^:const file-types-by-extension {:bindings  #{".edn"}
                                      :templates #{".art"}})

(defn identify-file-type
  "Given a file path, determine if the file is of a known file type or
  :unrecognized."
  [^String path]
  (let [is-of-type (fn [[k exts]]
                     (when (some #(str/ends-with? path %) exts)
                       k))]
    (or
      (some is-of-type file-types-by-extension)
      :unrecognized)))

(defn classify-paths
  "Sorts file paths into collections in a map keyed either by a known
  file type or :unrecognized."
  ([paths]
   (let [m (into {:unrecognized []} (for [k (keys file-types-by-extension)]
                                      [k []]))
         sorted (classify-paths paths m)]
     sorted))
  ([paths m]
   (reduce #(update %1 (identify-file-type %2) conj %2) m paths)))

(defn args->project-stanza
  "Convert Leiningen 'art' task command-line arguments into the
  equivalent project stanza."
  [args]
  (let [{:keys [arguments errors options]} (clojure.tools.cli/parse-opts args cli-options)
        {:keys [delimiters output-dir to-phase]} options]
    (when errors
      (main-lein/abort errors))
    (when (empty? (str/trim output-dir))
      (main-lein/abort ":output-dir is a required setting"))
    (let [sorted-paths (classify-paths arguments)
          unrecognized (:unrecognized sorted-paths)]
      (when (seq? unrecognized)
        (main-lein/warn (format "Warning: Ignoring unrecognized files: %s"
                                (str/join ", " unrecognized))))
      {:templates  (:templates sorted-paths)
       :bindings   (:bindings sorted-paths)
       :delimiters (rslv/resolve-delimiters delimiters)
       :output-dir output-dir
       :to-phase   to-phase})))
