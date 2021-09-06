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

(ns vivid.art.cli.log
  "Logging facility used by this library. Implemented by its consumers by
  binding these vars to actual functions.")

(defn ^:dynamic *info-fn*
  [& _]
  (throw (IllegalStateException. (str "First, bind " (var *info-fn*) " to a function."))))

(defn ^:dynamic *warn-fn*
  [& _]
  (throw (IllegalStateException. (str "First, bind " (var *warn-fn*) " to a function."))))
