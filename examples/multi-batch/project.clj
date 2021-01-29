;; This Leiningen project demonstrate defining multiple ART
;; render batch configurations.
;;
;; Run with:
;;
;;   $ lein art
;;   Rendering ART theme.css
;;   Rendering ART com/acme/Identity.java
;;   $ diff -r expected-src-resources/ src/resources/
;;   $ diff -r expected-target-generated-sources-java/ target/generated-sources/java/

(defproject art-example--multi-batch "0"

  ; Add the lein-art Leiningen plugin:
  :plugins [[vivid/lein-art "0.6.0"]]

  ; Two ART render batches are defined here:
  :art [
    ; An ART render batch configuration
    {:templates    "src/templates/css"
     :dependencies {garden {:mvn/version "1.3.10"}}
     :output-dir   "src/resources"}

    ; Another, different batch
    {:templates  ["src/templates/java"]
     :bindings   {version "1.2.3"}
     :output-dir "target/generated-sources/java"}])
