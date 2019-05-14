; Copyright 2019 Vivid Inc.

(ns vivid.art.failure
  (:require
    [clojure.spec.alpha :as s]
    [vivid.art.specs]))

(defn failure?
  "When ART fails to render a template, instead of the template output,
  it produces a value that describes the failure. Use this function to
  discriminate a failure from regular template output."
  [result]
  (when (s/valid? :vivid.art/failure result)
    result))

(defn make-failure
  "Makes an ART failure data structure describing the type of failure,
  the input that triggered the failure, and information about the cause."
  [failure-type cause template]
  {:failure-type failure-type
   :cause        cause
   :template     template})
(s/fdef make-failure
        :ret :vivid.art/failure)
