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

(ns vivid.art.parallel-test
  (:require
    [clojure.core.async :as async]
    [clojure.test :refer [is deftest testing]]
    [vivid.art :as art]))

; Referencing https://github.com/clojure/core.async/blob/master/examples/walkthrough.clj

(def ^:const +iterations+ 192)
(def ^:const nested-template "nested-X<X(X=X ctr X)X>X")
(def ^:const template (str "<( (def nested-template (.replaceAll \"" nested-template "\" \"X\" \"\")) )>template-<(= ctr )> <(= (render nested-template) )>"))

(defn actual [ctr]
  (art/render template
              :bindings {'ctr ctr}))
(defn expected [ctr]
  (format "template-%d nested-%d" ctr ctr))

(deftest re-entrant-property

  (testing "(render) is re-entrant with clojure.core (pmap)"
    (doall
      (pmap #(is (= (actual %) (expected %)))
            (range +iterations+))))

  (testing "(render) is re-entrant with clojure.core.async (go) and a single channel"
    (let [ch (async/chan)]
      (dotimes [ctr +iterations+]
        (async/go (async/>! ch {:ctr      ctr
                                :actual   (actual ctr)
                                :expected (expected ctr)})))
      (dotimes [_ +iterations+]
        (async/<!! (async/go (let [result (async/<! ch)]
                               (is (= (:actual result) (:expected result)))))))))

  (testing "(render) is re-entrant with clojure.core.async (thread) and a single channel"
    (let [ch (async/chan)]
      (dotimes [ctr +iterations+]
        (async/thread (async/>!! ch {:ctr      ctr
                                     :actual   (actual ctr)
                                     :expected (expected ctr)})))
      (dotimes [_ +iterations+]
        (async/<!! (async/thread (let [result (async/<!! ch)]
                                   (is (= (:actual result) (:expected result))))))))))
