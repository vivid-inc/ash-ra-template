; Copyright 2019 Vivid Inc.

(ns vivid.art.specs
  "Clojure Spec definitions for Ash Ra Temple data structures.
  All definitions exposed through ART's API plus all key
  definitions are namespaced."
  (:require
    [clojure.spec.alpha :as s]
    [clojure.tools.deps.alpha.specs]))


; ART templates

(s/def :vivid.art/template (s/nilable string?))


; Bindings

(s/def :vivid.art/bindings (s/nilable map?))


; Clojure dependency maps, required to render a given template

(s/def :vivid.art/dependencies :clojure.tools.deps.alpha.specs/deps-map)


; Failure descriptors

(s/def ::failure-type keyword?)
(s/def ::cause coll?)

(s/def :vivid.art/failure
  (s/keys :req-un [::failure-type ::cause ::template]))


; Template delimiter definitions

(s/def ::delimiter (s/and string? seq))

(s/def ::begin-forms ::delimiter)
(s/def ::end-forms ::delimiter)
(s/def ::begin-eval ::delimiter)
(s/def ::end-eval ::delimiter)

(s/def :vivid.art/delimiters
  (s/keys :opt-un [::begin-forms ::end-forms
                   ::begin-eval ::end-eval]))
