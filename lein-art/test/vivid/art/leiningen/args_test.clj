; Copyright 2019 Vivid Inc.

(ns vivid.art.leiningen.args-test
  (:require
    [clojure.edn]
    [clojure.java.io :as io]
    [clojure.string]
    [clojure.test :refer :all]
    [leiningen.art :as lein-art]
    [leiningen.core.project :as lein-prj]
    [vivid.art.delimiters]
    [vivid.art.leiningen.exec]
    [vivid.art.leiningen.resolve :as rslv])
  (:import
    (java.io File)
    (java.net URL)))

(defn delete-files-recursively [fname & [silently]]
  ((fn del [^File file]
     (when (.isDirectory file)
       (doseq [child (.listFiles file)]
         (del child)))
     (clojure.java.io/delete-file file silently))
    (io/file fname)))

(def test-project (lein-prj/read (^URL .getFile (io/resource "test-project.clj"))))

(defn project-stanza->cli-args
  [{:keys [templates bindings delimiters output-dir]}]
  (let [cc (fn [xs x] (concat xs (if (coll? x) x [x])))]
    (cond-> []
            templates (cc templates)
            bindings (cc bindings)
            delimiters (cc ["--delimiters" delimiters])
            output-dir (cc ["--output-dir" output-dir]))))

(defn call-art-via-cli-args
  [project-stanza delimiters output-dir]
  (let [s (merge project-stanza
                 (when delimiters
                   {:delimiters delimiters})
                 {:output-dir output-dir})
        args (concat [nil] (project-stanza->cli-args s))]
    (apply lein-art/art args)))

(defn call-art-via-project-stanza
  [project-stanza delimiters output-dir]
  (let [s (merge project-stanza
                 (when delimiters
                   {:delimiters (rslv/resolve-delimiters delimiters)})
                 {:output-dir output-dir})
        prj {:art s}]
    (lein-art/art prj)))

(defn invocation-pattern
  [data call-fn call-name]
  (testing call-name
    (let [{:keys [dir expected actual stanza delimiters]} data
          target-dir (str "target/art-rendered/" dir "-" call-name)]
      (delete-files-recursively target-dir :silently)
      (call-fn stanza delimiters target-dir)
      (is (= (slurp expected)
             (slurp (str target-dir "/" actual)))))))

(def calls
  [[call-art-via-cli-args "cli-args"]
   [call-art-via-project-stanza "project-stanza"]])

(defn all-invocation-patterns
  [data]
  (doseq [[call-fn call-name] calls]
    (invocation-pattern data call-fn call-name)))

(deftest default-delimiters
  (all-invocation-patterns
    {:dir      "greek"
     :expected "test-resources/greek/greek-expected.txt"
     :actual   "greek.txt"
     :stanza   {:templates ["test-resources/greek/greek.txt.art"]
                :bindings  ["test-resources/greek/greek.edn"]}}))

(deftest unqualified-name-of-default-delimiters
  (all-invocation-patterns
    {:dir        "greek-erb"
     :expected   "test-resources/greek/greek-expected.txt"
     :actual     "greek.txt"
     :stanza     {:templates ["test-resources/greek/greek.txt.art"]
                  :bindings  ["test-resources/greek/greek.edn"]}
     :delimiters 'erb}))

(deftest delimiters-edn
  (all-invocation-patterns
    {:dir        "greek-edn"
     :expected   "test-resources/greek/greek-expected.txt"
     :actual     "greek.txt"
     :stanza     {:templates ["test-resources/greek/greek.txt.art"]
                  :bindings  ["test-resources/greek/greek.edn"]}
     :delimiters (pr-str vivid.art.delimiters/erb)}))

(deftest qualified-name-of-bundled-delimiter-set
  (all-invocation-patterns
    {:dir        "japanese-jinja"
     :expected   "test-resources/japanese/japanese-expected.csv"
     :actual     "japanese.csv"
     :stanza     {:templates ["test-resources/japanese/japanese.csv.art"]}
     :delimiters 'vivid.art.delimiters/jinja}))



; TODO Error handling:
;
; non-existent template file
; compile error
;
;   0 templates
;   missing template filename
;   no read permission
;   missing binding filename
;   no read permission
;   template compile error
;   binding read error
;
;   dependencies:
;     malformed
;     resolution error
;#_(def ^:const dependencies
;    {'hiccup {:mvn/version "1.0.5"}})
;
;   delimiters:
;     qualified var cannot be resolved
