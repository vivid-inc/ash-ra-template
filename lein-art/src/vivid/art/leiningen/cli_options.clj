; Copyright 2019 Vivid Inc.

(ns vivid.art.leiningen.cli-options
  (:require
    [vivid.art]))

(def cli-options
  [;; --bindings are passed through to ART
   ["-b" "--bindings EDN-OR-VAR" "Bindings made available to templates for symbol resolution"]

   ;; --delimiters is passed through to ART
   ["-d" "--delimiters EDN-OR-VAR" "Template delimiters"
    :default vivid.art/default-delimiters-name]

   ;; --dependencies is passed through to ART
   [nil "--dependencies EDN-OR-VAR" "Clojure deps map"]

   ;; lein-art is responsible for writing rendered output to --output-dir
   ["-o" "--output-dir DIR" "Write rendered files to DIR"
    ;; Ideally the target path value would be derived from
    ;; (get leiningen.core.project/defaults :target-path)
    ;; but, due to some mechanic I don't fully understand, attempting to use
    ;; leiningen.core.project to render this project's README.md from
    ;; a template fails.
    :default "target"]

   ;; --to-phase is passed through to ART
   ["-p" "--to-phase KEYWORD" "Render dataflow on each template stop once the named phase is complete"
    :default vivid.art/default-to-phase]
   ])
