;; Run with:
;;
;;   $ lein do clean, install, art
;;   ...
;;   Rendering ART catalog/index.html
;;   $ diff -r expected/ out/cdn/

(defproject example-custom-options "0"

  :plugins [[net.vivid-inc/lein-art "0.7.0"]]

  ; Render all .art template files in the content/ directory to out/cdn/
  :art {:templates    "content"

        :bindings     [{manufacturer     "Acme Corporation"    ; Map literal
                        manufacture-year "2022"}

                       ; TODO #'com.acme.data/widget                 ; Var, value is a map
                       ; Its value is copy & pasted here:
                       {products [{:name               "Bag of bird seed"
                                    :weight-kgs         1.0
                                    :minimum-order-qty  50
                                    :unit-price-dollars 0.39M}
                                   {:name               "Ironing board on rollerskates"
                                    :weight-kgs         2.0
                                    :minimum-order-qty  10
                                    :unit-price-dollars 17.95M}]}

                       "{current-year 2021}"                   ; EDN as a string
                       "data/sales-offices.edn"                ; EDN file; top-level form is a map
                       "data/partner-list.json"]               ; JSON file; file content is made available under the symbol 'partner-list


        :delimiters   "jinja"                                  ; Resolves to #'vivid.art.delimiters/jinja

        :dependencies [[hiccup/hiccup "1.0.5" :exclusions [org.clojure/clojure]]]

        :output-dir   "out/cdn"})
