(set-env! :dependencies '[[net.vivid-inc/boot-art "0.6.1"]]
          :source-paths #{"src"}           ; Give templates use of project code
          :resource-paths #{"content"})    ; Render all .art templates in the content/ directory

(require '[clojure.java.io :as io]
         '[com.acme.data]
         '[vivid.boot-art :refer [art]])

(deftask rndr
  "Render all .art template files in the content/ directory to out/cdn/"
  []
  (comp (art :bindings     [{'manufacturer     "Acme Corporation"   ; Map literal
                             'manufacture-year "2022"}
                             #'com.acme.data/product-data           ; Var, value is a map
                             "{current-year 2021}"                  ; EDN as a string
                             "data/sales-offices.edn"               ; EDN file; top-level form is a map
                             "data/partner-list.json"]              ; JSON file; file content is made available under the symbol 'partner-list
             :delimiters   'jinja                                   ; Unqualified, resolves to #'vivid.art.delimiters/jinja
             :dependencies '{hiccup {:mvn/version "1.0.5"}})

        (target :dir #{"out/cdn"})))
