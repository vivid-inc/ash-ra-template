; Copyright 2019 Vivid Inc.

(ns vivid.art.failure)

(defn failure?
  "Returns a map (which is also a truthy value) describing the
  failure when the template render result is a failure."
  [result]
  (get result :vivid.art/failure))

(defn make-failure
  "Makes an ART failure data structure describing the type of failure,
  the input that triggered the failure, and information about the cause."
  [failure-type input cause]
  {:vivid.art/failure failure-type
   :input             input
   :cause             cause})
