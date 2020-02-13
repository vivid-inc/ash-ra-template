; Copyright 2019 Vivid Inc.

(ns leiningen.art
  (:require
    [vivid.art.leiningen.cli :refer [args->project-stanza]]
    [vivid.art.leiningen.exec :refer [run-lein-configuration]]))

(defn ^:no-project-needed art
  "Render Ash Ra ART templates.

Provided one or more template files and any quantity of optional bindings, this
Leiningen task writes rendered output to a specified output dir.

From within a Leiningen project:

  {:art {:templates    COLL-OF-FILES
         :bindings     SEQ-OF-MAP-VAR-EDN-FILE
         :delimiters   MAP
         :dependencies MAP
         :output-dir   DIR}}

Command-line usage:

  $ lein art [templates-and-bindings ...] <options>

and options:

  -b, --bindings EDN-OR-VAR              Bindings made available to templates for symbol resolution
  -d, --delimiters EDN-OR-VAR    erb     Template delimiters
      --dependencies EDN-OR-VAR          Clojure deps map
  -o, --output-dir DIR           target  Write rendered files to DIR

art takes a list of file paths to .art files (ART templates) and bindings.
CLI arguments can be freely mixed, but bindings are processed in order of
appearance which might be important to you in case of collisions.
From the CLI, the order that arguments are attempted to be resolved in is:
file path, EDN string, (un-)qualified name of a var.
Options that accept EDN-OR-VAR values will either accept EDN strings or
the namespace (un-)qualified name of a var.
output-dir will be created if necessary, and output files will overwrite
existing files with the same names.
Rendered output files are written to output-dir stripped of their .art
filename suffixes.

For more information, see https://github.com/vivid-inc/ash-ra-template"
  [project & args]
  (let [stanza (if (coll? args)
                 (args->project-stanza args)
                 (get project :art))]
    (run-lein-configuration stanza)))
