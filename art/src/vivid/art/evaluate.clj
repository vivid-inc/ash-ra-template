; Copyright 2019 Vivid Inc.
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

(ns vivid.art.evaluate)

(defmulti evaluate-fn
  "Internal API. Function in the templating processing pipeline that is
   responsible for evaluating the stream. This multimethod decl exists
   so that the sandbox variant of the evaluator defined in art-cli can
   be used whenever :dependencies are defined in the options map."
  (fn [_ options]
    (contains? options :dependencies)))

(defmethod evaluate-fn :default
  [code _]
  (locking *out*
    (load-string code)))

(defn evaluate
  [code render-options]
  (evaluate-fn code render-options))
