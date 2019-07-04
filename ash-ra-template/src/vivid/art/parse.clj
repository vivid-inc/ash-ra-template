; Copyright 2019 Vivid Inc.

(ns vivid.art.parse
  (:require
    [instaparse.core :as insta]
    [special.core :refer [condition]]
    [vivid.art.specs]
    [clojure.spec.alpha :as s]
    [clojure.string :as str])
  (:import
    (java.util.regex Pattern)))

(defn make-grammar                                          ; TODO Enforce delimiter rules.
  [delimiters]
  (let [q #(Pattern/quote %)
        alts (str/join " | " (map name (keys delimiters)))
        terminals (str/join
                    (interleave (map (fn [[k v]] (format "%s = '%s'"
                                                         (name k) v))
                                     delimiters)
                                (repeat "\n")))
        non-terms (str/join "|"
                            (map q (vals delimiters)))]
    (str "s = (" alts " | content)*\n"
         terminals
         "content = #'(?s)(?:(?!" non-terms ").)*'")))

; TODO Pass all tests from: clostache, mustache, etc.

(def ^:const tree-transformation
  {;; Strip the grammar starting rule from the token stream
   :s           (fn [& xs] xs)
   ;; Template tokens appear as namespaced keywords
   :begin-eval  (fn [_] :vivid.art/begin-eval)
   :begin-forms (fn [_] :vivid.art/begin-forms)
   :end-eval    (fn [_] :vivid.art/end-eval)
   :end-forms   (fn [_] :vivid.art/end-forms)
   ;; Inline string content
   :content     str})

(defn confirm-parse-output
  "Either raise a condition on parse error, or allow
   parse-result to pass through."
  [parse-result]
  (if (insta/failure? parse-result)
    (condition :parse-error (insta/get-failure parse-result))
    parse-result))

(defn parse
  "Tokenize a template string into a sequence of tokens suitable for parsing.

  Each lexeme is any of:
  - Template content, as a string.
  - Template delimiter, as a Clojure keyword."
  [^String template-str
   delimiters]
  (let [grammar (make-grammar delimiters)
        parser (insta/parser grammar)]
    (->> template-str
         (insta/parse parser)
         (confirm-parse-output)
         (insta/transform tree-transformation))))
(s/fdef parse
        :args (s/cat :template-str string?
                     :delimiters :vivid.art/delimiters)
        :ret seq?)
