; Copyright 2019 Vivid Inc.

(ns vivid.art.evaluate
  (:require
    [clojure.string]
    [vivid.art.embed :as embed]))

(def ^:const prelude ["(ns user)"])
(def ^:const interlude ["(def ^java.lang.StringBuilder __vivid__art__sb (new java.lang.StringBuilder))"
                        "(defn emit ([]) ([v] (.append user/__vivid__art__sb v) nil))"])
(def ^:const coda ["(.toString user/__vivid__art__sb)"])

(defn define-bindings
  [bindings]
  (vec (for [[k v] bindings] (format "(def %s %s)"
                                     (pr-str k)
                                     (pr-str v)))))

(defn evaluate
  [forms bindings dependencies]
  (let [code (->> (interpose [""]
                             [prelude
                              (define-bindings bindings)
                              interlude
                              forms
                              coda])
                  (flatten)
                  (clojure.string/join "\n"))]
    (embed/eval-in-one-shot-runtime code dependencies)))
