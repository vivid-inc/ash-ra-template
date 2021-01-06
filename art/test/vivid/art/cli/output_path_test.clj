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

(ns vivid.art.cli.output-path-test
  (:require
    [clojure.string]
    [clojure.test :refer :all]
    [vivid.art.cli.exec]
    [vivid.art.cli.test-lib :refer [special-manage-unwind-on-signal]]
    [vivid.art.cli.usage])
  (:import
    (java.io File)))

(deftest output-dir-cli-args
  (are [args dir]
    (let [expected (File. ^String dir)
          {:keys [output-dir]} (vivid.art.cli.args/cli-args->batch args vivid.art.cli.usage/cli-options)]
     (= expected
        output-dir))

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
  (let [f #(vivid.art.cli.args/cli-args->batch
             ["--output-dir" "" "test-resources/empty.art"]
             vivid.art.cli.usage/cli-options)
        {:keys [step]} (special-manage-unwind-on-signal f :vivid.art.cli/error)]
    (is (= 'validate-output-dir step))))

(deftest output-paths
  (are [^String template-base ^String template-path output-dir expected-output-path]
    (= expected-output-path
       (str (vivid.art.cli.exec/template-output-path
              (File. template-base)
              (File. template-path)
              output-dir)))
    "a.csv.art" "a.csv.art" "target" "target/a.csv"
    "/a.csv.art" "/a.csv.art" "target" "target/a.csv"

    "templates" "templates/b.txt.art" "target" "target/b.txt"
    "templates/" "templates/b.txt.art" "target" "target/b.txt"

    "/templates" "/templates/c.html.art" "target" "target/c.html"
    "/templates/" "/templates/c.html.art" "target" "target/c.html"

    "site/source" "site/source/about/d.properties.art" "rendered" "rendered/about/d.properties"
    "site/source" "site/source/about/d.properties.art" "/rendered" "/rendered/about/d.properties"

    "a/b/c" "a/b/c/d/e/f/g/h.sql.art" "out" "out/d/e/f/g/h.sql"
    "a/b/c/d/e" "a/b/c/d/e/recipe.xml.art" "/out" "/out/recipe.xml"))
