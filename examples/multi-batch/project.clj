; Copyright 2023 Vivid Inc. and/or its affiliates.
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

;; This Leiningen project demonstrate defining multiple ART
;; render batch configurations.
;;
;; Run with:
;;
;;   $ lein art
;;   Rendering ART theme.css
;;   Rendering ART com/acme/Identity.java
;;   $ diff -r expected-src-resources/ src/resources/
;;   $ diff -r expected-target-generated-sources-java/ target/generated-sources/java/

(defproject art-example--multi-batch "0"

  ; Add the lein-art Leiningen plugin:
  :plugins [[net.vivid-inc/lein-art "0.7.0"]]

  ; Two ART render batches are defined here:
  :art [
    ; An ART render batch configuration
    {:templates    "src/templates/css"
     :dependencies [[garden/garden "1.3.10"]]
     :output-dir   "src/resources"}

    ; Another, different batch
    {:templates    ["src/templates/java"]
     :bindings     {version "1.2.3"}
     :output-dir   "target/generated-sources/java"}

    ; One more batch
    {:templates    "src/templates/html"
     :dependencies [[hiccup/hiccup "1.0.5"]]
     :output-dir   "www"}])
