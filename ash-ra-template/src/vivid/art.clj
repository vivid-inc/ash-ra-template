; Copyright 2019 Vivid Inc.

(ns vivid.art
  "Ash Ra Template public API."
  (:require
    [clojure.spec.alpha :as s]
    [clojure.string]
    [special.core :refer [condition manage]]
    [vivid.art.delimiters :refer [erb]]
    [vivid.art.evaluate :refer [evaluate]]
    [vivid.art.failure :refer [make-failure]]
    [vivid.art.parse :refer [parse]]
    [vivid.art.specs]
    [vivid.art.xlate :refer [translate]]))

(def ^:const failure? vivid.art.failure/failure?)

(defn render
  "Renders an input string containing Ash-Ra Template
  -formatted content to an output string."
  [^String template
   & {:keys [delimiters
             dependencies]
      :or   {delimiters erb}}]
  (if template
    (let [render* #(-> template
                       (parse delimiters)
                       (translate)
                       (evaluate :dependencies dependencies))
          f (manage render*
                    :parse-error #(make-failure :parse-error % template))]
      (f))))
(s/fdef render
        :args (s/cat :t :vivid.art/template
                     :o (s/keys* :opt-un [:vivid.art/delimiters
                                          :vivid.art/dependencies])))
