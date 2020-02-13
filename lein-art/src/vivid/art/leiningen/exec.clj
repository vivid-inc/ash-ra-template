; Copyright 2019 Vivid Inc.

(ns vivid.art.leiningen.exec
  (:import
    (java.io File))
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [leiningen.core.main :as main-lein]
    [vivid.art :as art]
    [vivid.art.leiningen.resolve :as rslv]))


(defn path-seq
  [^File path]
  (some->> path
           (.getCanonicalFile)
           (.toPath)
           (.iterator)
           (iterator-seq)
           (map str)))

(defn project-stanza->conf
  [stanza]
  (update stanza :bindings #(->> (rslv/resolve-bindings %)
                                 (apply merge))))

(defn remove-common-heads
  [x-seq y-seq]
  (loop [xs x-seq
         ys y-seq]
    (if (and (not (empty? xs)) (not (empty? ys))
             (= (first xs) (first ys)))
      (recur (rest xs) (rest ys))
      [xs ys])))

(defn relative-path
  "path is assumed to be a file"
  [^File base ^File path]
  (let [b (path-seq base)
        p (path-seq path)
        [_ rel-path] (remove-common-heads b p)]
    rel-path))

(defn strip-art-filename-suffix
  [f]
  (str/replace f art/art-filename-suffix-regex ""))

(defn template-file-seq
  [^File path]
  (filter #(and (.isFile ^File %)
                (.endsWith (.getName ^File %) art/art-filename-suffix)) (file-seq path)))

(defn template-output-path
  [^File base ^File path output-dir]
  (let [rel-path-parent (relative-path base (.getParentFile path))
        rendered-output-filename (strip-art-filename-suffix (.getName path))
        output-path (apply io/file (concat [output-dir]
                                           rel-path-parent
                                           [rendered-output-filename]))]
    output-path))


(defn render-file
  [^File templates-base ^File template-file {:keys [output-dir] :as conf}]
  (let [raw (slurp template-file)
        rendered (art/render raw (select-keys conf [:bindings :delimiters :dependencies]))
        output-file (template-output-path templates-base template-file output-dir)]
    (io/make-parents output-file)
    (spit output-file rendered)
    (main-lein/info "Rendered ART" (str output-file))))

(defn render-templates-base
  [t conf]
  (let [templates-base (io/file t)
        template-files (template-file-seq templates-base)]
    (doseq [template-file template-files]
      (render-file templates-base template-file conf))))

(defn run-lein-configuration-stanza
  [{:keys [templates] :as stanza}]
  (if (empty? templates)
    (main-lein/warn "Warning: No ART templates to render.")
    (let [conf (project-stanza->conf stanza)]
      (doseq [t templates]
        (render-templates-base t conf)))))

(defn run-lein-configuration
  [lein-conf]
  (cond
    (map? lein-conf) (run-lein-configuration-stanza lein-conf)
    (coll? lein-conf) (doseq [stanza lein-conf]
                        (run-lein-configuration-stanza stanza))
    :else (main-lein/warn "Warning: Unknown ART configuration")))
