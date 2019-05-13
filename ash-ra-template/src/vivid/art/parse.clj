; Copyright 2019 Vivid Inc.

(ns vivid.art.parse
  (:require
    [instaparse.core :as insta]
    [special.core :refer [condition]]
    [vivid.art.specs]
    [clojure.spec.alpha :as s])
  (:import
    (java.util.regex Pattern)))

(defn make-grammar
  [delimiters]
  (let [q #(Pattern/quote %)
        {:keys [begin-forms end-forms begin-eval]} delimiters]
    (str "s = (begin-eval | begin-echo-eval | end | content)*
begin-eval = '" begin-forms "'
begin-echo-eval = '" begin-eval "'
end = '" end-forms "'
content = #'(?s)(?:(?!" (q begin-forms)
         "|" (q begin-eval)
         "|" (q end-forms)
         ").)*'")))

(def ^:const tree-transformation
  {;; Strip the grammar starting rule from the token stream
   :s               (fn [& xs] xs)
   ;; Template tokens appear as namespaced keywords
   :begin-eval      (fn [_] :vivid.art/begin-eval)
   :begin-echo-eval (fn [_] :vivid.art/begin-echo-eval)
   :end             (fn [_] :vivid.art/end)
   ;; Inline string content
   :content         str})

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
