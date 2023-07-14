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

(defproject art-example--all-options "0"

  ; Add the lein-art Leiningen plugin:
  :plugins [[net.vivid-inc/lein-art "0.7.0"]]

  ; Render .art templates
  :art {:bindings     {updated "2021-01-01"}
        :dependencies [[hiccup/hiccup "1.0.5" :exclusions [org.clojure/clojure]]]
        :delimiters   {:begin-forms "{%" :end-forms "%}" :begin-eval "{%=" :end-eval "%}"}
        :output-dir   "target"
        :templates    "templates"
        :to-phase     :evaluate})
