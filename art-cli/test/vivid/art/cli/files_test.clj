; Copyright 2024 Vivid Inc. and/or its affiliates.
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

(ns vivid.art.cli.files-test
  (:require
   [clojure.test :refer [are deftest]]
   [farolero.core :as farolero]
   [vivid.art.specs]
   [vivid.art.cli.files])
  (:import
   (java.io File)))

(deftest relative-paths
  (are [^String a ^String b res]
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
    "file" "file"
    "template.art" "template"))

(deftest strip-art-filename-suffixes-prohibited
  (are [filename]
       (= 'strip-art-filename-suffix
          (farolero/handler-case (vivid.art.cli.files/strip-art-filename-suffix filename)
                                 (:vivid.art.cli/error [_ {:keys [step]}] step)))
    "..art"                                                 ; Stripped to "."
    "...art"                                                ; Stripped to ".."
    ))

; TODO test bad globs

(deftest orient-path-specs
         (let [ks [:base-dir :oriented-as :pathmatcher-arg]
               o  (fn [oriented-as ^String base-dir-str glob]
                      {:base-dir        (File. base-dir-str)
                       :oriented-as     oriented-as
                       :pathmatcher-arg glob})]
              (are [path-spec res]
                   (= (select-keys res ks)
                      (select-keys (vivid.art.cli.files/orient-path-spec path-spec) ks))

                   ; Single files
                   ;
                   ; Implementation note: These need not exist within "test-resources/"  dir.
                   "template.art"                 (o :file "." "glob:template.art")
                   (str "file" vivid.art.cli.files/art-filename-suffix) (o :file
                                                                           "."
                                                                           (str "glob:file" vivid.art.cli.files/art-filename-suffix))
                   "docs/file.md.art"             (o :file "." "glob:docs/file.md.art")
                   "a/b/c/tmplt.asciidoc.art"     (o :file "." "glob:a/b/c/tmplt.asciidoc.art")
                   "../../faq.txt.art"            (o :file "." "glob:../../faq.txt.art")
                   "../a/../b/../a/../config.art" (o :file "." "glob:../a/../b/../a/../config.art")
                   "`~_-=+!/@#$%\\^^&|;/:\"',.<>///////a-file" (o :file
                                                                  "."
                                                                  "glob:`~_-=+!/@#$%\\^^&|;/:\"',.<>///////a-file")

                   ; Directory path specifications without globbing.
                   ;
                   ; Implementation note: Named directories must exist within "test-resources/" dir or navigable therefrom.
                   ; We expect that the default template glob is added implicitly.
                   "test"                     (o :directory "test" (str "glob:test/**" vivid.art.cli.files/art-filename-suffix))
                   "test/"                    (o :directory "test/" (str "glob:test/**" vivid.art.cli.files/art-filename-suffix))
                   "./test"                   (o :directory "./test" (str "glob:./test/**" vivid.art.cli.files/art-filename-suffix))
                   "test/"                    (o :directory "test" (str "glob:test/**" vivid.art.cli.files/art-filename-suffix))
                   "./test/"                  (o :directory "./test" (str "glob:./test/**" vivid.art.cli.files/art-filename-suffix))
                   "test/."                   (o :directory "test/." (str "glob:test/./**" vivid.art.cli.files/art-filename-suffix))
                   "./test/."                 (o :directory "./test/." (str "glob:./test/./**" vivid.art.cli.files/art-filename-suffix))
                   "./././test/././././."     (o :directory "./././test/././././." (str "glob:./././test/./././././**" vivid.art.cli.files/art-filename-suffix))
                   "test/../test/../test"     (o :directory "test/../test/../test" (str "glob:test/../test/../test/**" vivid.art.cli.files/art-filename-suffix))
                   "test/../test/../test/.."  (o :directory "test/../test/../test/.." (str "glob:test/../test/../test/../**" vivid.art.cli.files/art-filename-suffix))
                   "test-resources/dir-a/../dir-a/dir-b/../dir-b" (o :directory
                                                                     "test-resources/dir-a/../dir-a/dir-b/../dir-b"
                                                                     (str "glob:test-resources/dir-a/../dir-a/dir-b/../dir-b/**" vivid.art.cli.files/art-filename-suffix))

                   ; Globs
                   ;
                   ; Test each glob pattern type, oriented in the current working directory:
                   ; - * matches zero or more characters without crossing directory boundaries.
                   "*"                                (o :glob "." "glob:*")
                   (str "*" vivid.art.cli.files/art-filename-suffix) (o :glob
                                                                        "."
                                                                        (str "glob:*" vivid.art.cli.files/art-filename-suffix))
                   "*.art"                            (o :glob "." "glob:*.art")
                   "*.gzip"                           (o :glob "." "glob:*.gzip")
                   "*.*"                              (o :glob "." "glob:*.*")
                   "specification-*xml"               (o :glob "." "glob:specification-*xml")
                   "list*"                            (o :glob "." "glob:list*")
                   "a*bc*d*e"                         (o :glob "." "glob:a*bc*d*e")
                   "boot*!"                           (o :glob "." "glob:boot*!")
                   ; - ** matches zero or more characters crossing directory boundaries.
                   "**"                               (o :glob "." "glob:**")
                   "**/*"                             (o :glob "." "glob:**/*")
                   "svg**.svg"                        (o :glob "." "glob:svg**.svg")
                   "icons-**"                         (o :glob "." "glob:icons-**")
                   "a-**b**-**-c**"                   (o :glob "." "glob:a-**b**-**-c**")
                   ; - ? matches one character of a name component.
                   "?"                                (o :glob "." "glob:?")
                   "?.art"                            (o :glob "." "glob:?.art")
                   "??x0??"                           (o :glob "." "glob:??x0??")
                   "index-??.html.art"                (o :glob "." "glob:index-??.html.art")
                   "asset.??"                         (o :glob "." "glob:asset.??")
                   "?a?b??c???"                       (o :glob "." "glob:?a?b??c???")
                   ; - \ escapes characters that would otherwise be interpreted as special characters.
                   ; Implementation note: Written in a Java String literal, the escape char itself needs escaping with another \.
                   "all-\\*s"                         (o :file "." "glob:all-\\*s")
                   "predicates_lib\\?.dll"            (o :file "." "glob:predicates_lib\\?.dll")
                   "between\\[the!lines\\]"           (o :file "." "glob:between\\[the!lines\\]")
                   "no-accounting-\\{for-taste\\}"    (o :file "." "glob:no-accounting-\\{for-taste\\}")
                   ; - [ ] match a single character of a name component out of a set of chars.
                   ; Implementation note: - range and ! not need not be tested; outside the context of bracket expressions they have no effect.
                   "[abc]"                            (o :glob "." "glob:[abc]")
                   "[efg04v7z]"                       (o :glob "." "glob:[efg04v7z]")
                   "[x-z]-axis"                       (o :glob "." "glob:[x-z]-axis")
                   "db[0-9][0-9].db"                  (o :glob "." "glob:db[0-9][0-9].db")
                   "part-[a-m]"                       (o :glob "." "glob:part-[a-m]")
                   ; - { } match any in a group of sub-patterns.
                   "{java,class}"                     (o :glob "." "glob:{java,class}")
                   "{a,pe,ru,t,wo}nt"                 (o :glob "." "glob:{a,pe,ru,t,wo}nt")
                   "c{,hlorof,onf,uneif}orm"          (o :glob "." "glob:c{,hlorof,onf,uneif}orm")
                   "ex{tra,ude,ult}"                  (o :glob "." "glob:ex{tra,ude,ult}")
                   "{more,than}{,one}"                (o :glob "." "glob:{more,than}{,one}")
                   ; Test directory orientation with globs:
                   "indices/chunk-*-00.db.art"        (o :glob "indices" "glob:indices/chunk-*-00.db.art")
                   "root/branch/{left,right}/leaf"    (o :glob "root/branch" "glob:root/branch/{left,right}/leaf")
                   "deep/a/b/c/d/e/**/*.html.art"     (o :glob "deep/a/b/c/d/e" "glob:deep/a/b/c/d/e/**/*.html.art")
                   "[a-zA-Z]*.dir/ctx-{sub,super}/**/*.csv.art" (o :glob
                                                                   "."
                                                                   "glob:[a-zA-Z]*.dir/ctx-{sub,super}/**/*.csv.art")
                   "../../../dir/sub-dir/[0-9]/*.art" (o :glob "../../../dir/sub-dir" "glob:../../../dir/sub-dir/[0-9]/*.art")
                   "../in/../in/../in/../in/peer*"    (o :glob "../in/../in/../in/../in" "glob:../in/../in/../in/../in/peer*"))))
