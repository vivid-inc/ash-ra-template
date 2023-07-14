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

(ns vivid.art.cli.output-path-test
  (:require
   [clojure.string]
   [clojure.test :refer [are deftest is]]
   [farolero.core :as farolero]
   [vivid.art.cli.args]
   [vivid.art.cli.files]
   [vivid.art.cli.usage])
  (:import
   (java.io File)))

(deftest output-dir-cli-args
  (are [args ^String dir]
       (= (.getAbsoluteFile (File. dir))
          (-> (vivid.art.cli.args/cli-args->batch args vivid.art.cli.usage/cli-options)
              :output-dir))

    ["test-resources/empty.art" "--output-dir" "/"]
    "/"

    ["test-resources/empty.art" "--output-dir" ".."]
    ".."

    ["test-resources/empty.art" "--output-dir" "."]
    "."

    ["--output-dir" "target" "test-resources/empty.art"]
    "target"

    ["--output-dir" "../here/there" "test-resources/empty.art"]
    "../here/there"))

(deftest cli-empty-output-dir
  (is (= 'validate-output-dir
         (farolero/handler-case (vivid.art.cli.args/cli-args->batch
                                 ["--output-dir" "" "test-resources/empty.art"]
                                 vivid.art.cli.usage/cli-options)
                                (:vivid.art.cli/error [_ {:keys [step]}] step)))))

(deftest template-paths
  (are [^String base-path ^String template-file dest-rel-path]
       (= {:src-path (File. ^String template-file)
           :dest-rel-path (File. ^String dest-rel-path)}
          (vivid.art.cli.files/->template-path (File. base-path) (File. template-file)))
    "a.csv.art" "a.csv.art" "a.csv"
    "/a.csv.art" "/a.csv.art" "a.csv"

    "templates" "templates/b.txt.art" "b.txt"
    "templates/" "templates/b.txt.art" "b.txt"

    "/templates" "/templates/c.txt.art" "c.txt"
    "/templates/" "/templates/c.txt.art" "c.txt"

    "site/source" "site/source/about/d.properties.art" "about/d.properties"

    "a/b/c" "a/b/c/d/e/f/g/h.sql.art" "d/e/f/g/h.sql"
    "a/b/c/d/e" "a/b/c/d/e/recipe.xml.art" "recipe.xml"))
