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

(def ^:const art-filename-suffix ".art")
(def ^:const art-filename-suffix-regex #"\.art$")

(def ^:const default-delimiters-name "erb")
(def ^:const default-delimiters (var-get
                                  (ns-resolve 'vivid.art.delimiters
                                              (symbol default-delimiters-name))))

(def ^:const failure? vivid.art.failure/failure?)

(defn render
  "Renders an input string containing Ash Ra Template (ART) -formatted content
  to an output string."
  ([^String template] (render template {}))
  ([^String template
    {:keys [bindings delimiters dependencies]
     :or   {bindings {} delimiters default-delimiters dependencies {}}}]
   (if template
     (let [render* #(-> template
                        (parse delimiters)
                        (translate)
                        (evaluate bindings dependencies))
           f (manage render*
                     :parse-error #(make-failure :parse-error % template))]
       (f)))))
(s/fdef render
        :args (s/cat :t :vivid.art/template
                     :o (s/? (s/keys :opt-un [:vivid.art/bindings
                                              :vivid.art/delimiters
                                              :vivid.art/dependencies]))))
