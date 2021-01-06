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

(ns vivid.art.cli.files-test
  (:require
    [clojure.test :refer :all]
    [vivid.art.specs]
    [vivid.art.cli.files])
  (:import
    (java.io File)))

(deftest relative-paths
  (are [a b res]
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
    "..art" "."                                             ; TODO special-case "." and ".."
    "file" "file"
    "template.art" "template"))

(deftest template-file-seqs
  (are [path res]
    (= res
       (vivid.art.cli.files/template-file-seq (File. path)))
    "test-resources/template-file-seq" [(File. "test-resources/template-file-seq/template.art")]))
