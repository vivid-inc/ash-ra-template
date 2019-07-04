; Copyright 2019 Vivid Inc.

(ns vivid.art.leiningen.exec
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [leiningen.core.main :as main-lein]
    [vivid.art :as art]
    [vivid.art.leiningen.resolve :as rslv]))

(defn project-stanza->conf
  [stanza]
  (update stanza :bindings #(->> (rslv/resolve-bindings %)
                                 (apply merge))))

(defn strip-art-filename-suffix
  [f]
  (str/replace f art/art-filename-suffix-regex ""))

(defn render-template
  [template-file {:keys [output-dir] :as conf}]
  (let [raw (slurp template-file)
        rendered (art/render raw (select-keys conf [:bindings :delimiters :dependencies]))
        dest-filename (.getName (io/file (strip-art-filename-suffix template-file)))
        dest (io/file output-dir dest-filename)]
    (clojure.java.io/make-parents dest)
    (spit dest rendered)))

(defn render-templates
  [{:keys [templates] :as stanza}]
  (if (empty? templates)
    (main-lein/warn "Warning: No templates to render.")
    (let [conf (project-stanza->conf stanza)]
      (doseq [t templates]
        (render-template t conf)))))
