; Copyright 2020 Vivid Inc.
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;    https://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns vivid.art.cli.validate
  "Validation of individual options available in public API & CLI"
  (:require
    [clojure.spec.alpha :as s]
    [clojure.string]
    [special.core :as special]
    [vivid.art]
    [vivid.art.cli.resolve :as resolve]
    [vivid.art.specs]))

(defn validate-bindings
  "Is either a single or collection of binding maps. Each
  binding definition is a Clojure map, a Clojure var, a string path to an
  EDN file containing a map, or a stringified EDN literal map."
  [bindings]
  (reduce
    (fn [acc x]
      (as-> x b
            (or (resolve/resolve-as-map b)
                (resolve/resolve-as-var b)
                (resolve/resolve-as-edn-file b)
                (resolve/resolve-as-edn-literal b))
            (resolve/resolve-as-map b)
            (s/conform :vivid.art/bindings b)
            (if-not (s/invalid? b)
              (merge acc b)
              (special/condition :vivid.art.cli/error
                                 {:step    'validate-bindings
                                  :message (format "Bad bindings: '%s'" x)}))))
    {}
    ; Flattened, bindings will be fed to (reduce) as a collection if it wasn't one already.
    (flatten [bindings])
    ))

(defn validate-delimiters
  "ART is lenient in its acceptance of delimiter specifications."
  [x]
  (as-> x d
        (or (resolve/resolve-as-map d)
            (resolve/resolve-as-var d 'vivid.art.delimiters)
            (resolve/resolve-as-edn-literal d))
        (resolve/resolve-as-map d)
        (s/conform :vivid.art/delimiters d)
        (if-not (s/invalid? d)
          d
          (special/condition :vivid.art.cli/error
                             {:step    'validate-delimiters
                              :message (format "Non-conformant delimiter specification: '%s'" x)}))))

(defn validate-dependencies
  [x]
  (as-> x d
        (or (resolve/resolve-as-map d)
            (resolve/resolve-as-var d)
            (resolve/resolve-as-edn-file d)
            (resolve/resolve-as-edn-literal d))
        (resolve/resolve-as-map d)
        (s/conform :vivid.art/dependencies d)
        (if-not (s/invalid? d)
          d
          (special/condition :vivid.art.cli/error
                             {:step    'validate-dependencies
                              :message (format "Non-conformant dependency map: '%s'" x)}))))

(defn validate-output-dir
  "A string path of the output directory."
  [output-dir]
  (let [f (resolve/resolve-as-file output-dir)]
    (if f
      f
      (special/condition :vivid.art.cli/error
                         {:step    'validate-output-dir
                          :message (format "output-dir '%s' must name a directory path" output-dir)}))))

(defn validate-templates
  "Returns a collection of java.io.File's representing each of the named
  template file paths, being any mix of existing files and directories.
  Any unresolvable named path is special/condition'ed as an error."
  [x]
  (letfn [(conv [path]
            (let [f (resolve/resolve-as-file path)]
              (if (and f (.exists f))
                f
                (special/condition :vivid.art.cli/error
                                   {:step    'validate-templates
                                    :message (format "Template path doesn't exist: '%s'" path)}))))]
    (map conv (if (coll? x) x [x]))))

(defn validate-to-phase
  "The :to-phase option can either be a valid keyword or its string representation.
  Returns the valid keyword, otherwise nil."
  [x]
  (as-> x p
        (if (string? p)
          (keyword p)
          p)
        (s/conform :vivid.art/render-phase p)
        (if-not (s/invalid? p)
          p
          (special/condition :vivid.art.cli/error
                             {:step    'validate-to-phase
                              :message (format "to-phase '%s' is unknown; must be one of:  %s"
                                               x
                                               (clojure.string/join "  " (map name vivid.art/render-phases)))}))))
