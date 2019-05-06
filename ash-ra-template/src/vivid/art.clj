; Copyright 2019 Vivid Inc.

(ns vivid.art
  (:require
    [clojure.string]
    [vivid.art.delimiters]
    [vivid.art.evaluate :refer [evaluate]]
    [vivid.art.lex :refer [lex]]
    [vivid.art.parse :refer [parse]]))

(defn render
  "Renders an input string containing Ash-Ra Template
  -formatted content to an output string"
  [^String input
   & {:keys [delimiters
             dependencies]
      :or   {delimiters vivid.art.delimiters/erb}}]
  (-> input
      (lex delimiters)
      (parse)
      (evaluate :dependencies dependencies)))
