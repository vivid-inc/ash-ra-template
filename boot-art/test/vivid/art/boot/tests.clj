; Copyright 2019 Vivid Inc.

(ns vivid.art.boot.tests
  (:require
    [boot.core :as boot :refer [deftask]]
    [boot.test :refer :all]
    [clojure.java.io :as io]
    [clojure.string :as string]
    [vivid.boot-art :refer [art]])
  (:use
    [clojure.test :only [is testing]]))

(defn clj?
  [f]
  (string/ends-with? f ".clj"))

(defn fs-paths [fs]
  (->> fs
       (boot/ls)
       (map boot/tmp-path)
       (remove clj?)
       sort))

(defn fs-contents [fs]
  (->> fs
       (boot/ls)
       (remove (comp clj? boot/tmp-path))
       (sort-by boot/tmp-path)
       (map boot/tmp-file)
       (map slurp)))

(def placebo-content
  "Processing this content with ART will not change its contents as there are no ART directives.")

(def template-raw
  "
ART template <%=\"test\"%>
<%
(defn modular-arithmetic [a b z] (mod (+ a b) z))
(def z 11)
%>
8 + 7 is <%= (modular-arithmetic 8 7 z) %> in Z-<%=z%>.
")

(def template-rendered
  "
ART template test

8 + 7 is 4 in Z-11.
")

(deftask populate
         []
         (boot/with-pre-wrap fileset
                             (let [tmp (boot/tmp-dir!)
                                   files {"placebo.art"  placebo-content
                                          "template.art" template-raw}]
                               (doseq [[file content] files]
                                 (-> file
                                     (->> (io/file tmp))
                                     (doto io/make-parents)
                                     (spit content)))
                               (-> fileset
                                   (boot/add-resource tmp)
                                   boot/commit!))))

(deftask expect
         [_ paths VAL [str] "expected paths"
          _ contents VAL [str] "expected contents"]
         (boot/with-pass-thru fs
                              (testing "file paths"
                                (is (= paths (fs-paths fs))))
                              (testing "file contents"
                                (is (= contents (fs-contents fs))))))

(deftesttask no-rendering []
             (comp (populate)
                   (expect :paths ["placebo.art"
                                   "template.art"]
                           :contents [placebo-content
                                      template-raw])))

(deftesttask with-rendering []
             (comp (populate)
                   (art)
                   (expect :paths ["placebo"
                                   "template"]
                           :contents [placebo-content
                                      template-rendered])))
