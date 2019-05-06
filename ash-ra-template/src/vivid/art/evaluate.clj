; Copyright 2019 Vivid Inc.

(ns vivid.art.evaluate
  (:require
    [clojure.string]
    [vivid.art.embed :as embed]))

(defn wrap-forms
  [forms]
  (concat ["(ns user)"
           "(def ^java.lang.StringBuilder __vivid__art__sb (new java.lang.StringBuilder))"
           "(defn emit [val] (.append user/__vivid__art__sb val))"]
          forms
          ["(.toString user/__vivid__art__sb)"]))

(defn evaluate
  [forms
   & {:keys [dependencies]}]
  (let [wrapped-forms (wrap-forms forms)
        str (clojure.string/join "\n" wrapped-forms)]
    (embed/eval-in-one-shot-runtime str
                                    :dependencies dependencies)))
