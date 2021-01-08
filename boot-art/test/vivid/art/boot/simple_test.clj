; Copyright 2021 Vivid Inc.
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

(ns vivid.art.boot.simple-test
  (:require
    [boot.core :as boot]
    [boot.test]
    [clojure.java.io :as io]
    [clojure.string]
    [clojure.test :as t]
    [vivid.boot-art])
  (:import
    (java.io File)))

(defn clj-filename?
  [f]
  (clojure.string/ends-with? f ".clj"))

(defn fs-paths [fs]
  (->> fs
       (boot/ls)
       (map boot/tmp-path)
       (remove clj-filename?)
       sort))

(defn fs-contents [fs]
  (->> fs
       (boot/ls)
       (remove (comp clj-filename? boot/tmp-path))
       (sort-by boot/tmp-path)
       (map boot/tmp-file)
       (map slurp)))

(boot/deftask expect
              [_ paths VAL [str] "paths to files expected paths"]
              (boot/with-pass-thru fileset
                                   (let [expected-contents (map #(slurp (str % ".expected"))
                                                                paths)
                                         template-filenames (map #(-> % (File.) (.getName)) paths)]
                                     (t/testing "Rendered template file output path"
                                       (t/is (= template-filenames (fs-paths fileset))))
                                     (t/testing "Rendered template file output content"
                                       (t/is (= expected-contents (fs-contents fileset)))))))

(boot/deftask populate
              "Populates the Boot working set of files with templates"
              [_ paths VAL [str] "template file paths"]
              (boot/with-pre-wrap fileset
                                  (let [tmp-dir (boot/tmp-dir!)
                                        template-paths (map #(str % vivid.art/art-filename-suffix)
                                                            paths)]
                                    (doseq [path template-paths]
                                      (let [content (slurp path)]
                                        (-> (File. path)
                                            (.getName)
                                            (->> (io/file tmp-dir))
                                            (doto io/make-parents)
                                            (spit content))))
                                    (-> fileset
                                        (boot/add-resource tmp-dir)
                                        boot/commit!))))

(boot.test/deftesttask
  simple []
  (let [paths ["../art/test-resources/simple/no-syntax.txt"
               "../art/test-resources/simple/template.txt"]]
    (comp (populate :paths paths)
          (vivid.boot-art/art)
          (expect :paths paths))))
