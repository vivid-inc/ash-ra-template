; Copyright 2024 Vivid Inc. and/or its affiliates.
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     https://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns ^:internal-api vivid.art.cli.render+
  (:require
   [vivid.art :as art])
  (:import
   (java.io File)
   (java.nio.file Files Path)))

(set! *warn-on-reflection* true)

;
; Expand on art/Render protocol with types apropos to CLI-based operations
;

(defn render-template-at-filesystem-path
  "Reads in as a string the contents of the file at path on the filesystem
  (assuming the default character encoding) and renders it."
  [^Path path options]
  (let [template (String. (Files/readAllBytes path))]
    (art/render-template-string template options)))

(extend-protocol art/Render
  File
  (render
    ([^File file] (render-template-at-filesystem-path (.toPath file) nil))
    ([^File file options] (render-template-at-filesystem-path (.toPath file) options)))

  Path
  (render
    ([^Path path] (render-template-at-filesystem-path path nil))
    ([^Path path options] (render-template-at-filesystem-path path options))))
