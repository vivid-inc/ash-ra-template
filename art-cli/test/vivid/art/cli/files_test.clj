; Copyright 2022 Vivid Inc. and/or its affiliates.
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

(ns vivid.art.cli.files-test
  (:require
    [clojure.test :refer [are deftest]]
    [farolero.core :as farolero]
    [vivid.art.specs]
    [vivid.art.cli.files])
  (:import
    (java.io File)))

(deftest relative-paths
  (are [^String a ^String b res]
    (= res
       (vivid.art.cli.files/relative-path (File. a) (File. b)))
    "" "" ()
    "a/b/c" "a/b/c/y/z" '("y" "z")))

(deftest strip-art-filename-suffixes
  (are [in out]
    (= out
       (vivid.art.cli.files/strip-art-filename-suffix in))
    "" ""
    ".art" ""
    " .art" " "
    "file" "file"
    "template.art" "template"))

(deftest strip-art-filename-suffixes-prohibited
  (are [filename]
    (= 'strip-art-filename-suffix
       (farolero/handler-case (vivid.art.cli.files/strip-art-filename-suffix filename)
                              (:vivid.art.cli/error [_ {:keys [step]}] step)))
    "..art"                                                 ; Stripped to "."
    "...art"                                                ; Stripped to ".."
    ))

(deftest template-file-seqs
  (are [^String path res]
    (= res
       (vivid.art.cli.files/template-file-seq (File. path)))
    "../examples/all-options/templates"
    [(File. "../examples/all-options/templates/header.html.art")]))
