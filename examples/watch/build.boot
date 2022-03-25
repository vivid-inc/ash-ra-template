;; Run with:
;;
;;   $ boot watch art target
;; or
;;   $ boot dev
;;
;; Then edit resources/template.txt.art and expect:
;;
;;   Rendering ART template.txt
;;   Writing target dir(s)...

(set-env! :dependencies '[[net.vivid-inc/boot-art "0.7.0"]]
          :resource-paths #{"resources"})    ; Render all .art templates in the content/ directory

(require '[vivid.boot-art :refer [art]])

(deftask dev
  "Development mode: Render all .art template files in the resources/ directory to target/
  whenever they change."
  []
  (comp (watch)
        (art)
        (target)))
