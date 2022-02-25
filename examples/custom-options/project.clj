;; Run with:
;;
;;   $ lein do clean, install, art
;;   ...
;;   Rendering ART catalog/index.html
;;   $ diff -r expected/ out/cdn/

(defproject example-custom-options "0"

  :plugins [[net.vivid-inc/lein-art "0.6.1"]]

  ; Render all .art template files in the content/ directory to out/cdn/
  :art {:templates    "content"

        :bindings     [{manufacturer     "Acme Corporation"    ; Map literal
                        manufacture-year "2022"}

                       ; NOTE: The authors that have touched this file so far don't know
                       ; how to specify a Var that is defined within src/ as in:
                       ;   #'com.acme.data/widget                 ; Var, value is a map
                       ; So its value is copy & pasted here:
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

        :dependencies {hiccup {:mvn/version "1.0.5"}
                       ; Give templates use of project code.
                       ; Note that ART's Clojure Deps -based dependency resolver
                       ; requires a `deps.edn` or `pom.xml` file in
                       ; order to recognize this "." project as a dependency.
                       ; The name is the same as this Lein project.
                       ; Use a version spec that suites your needs.
                       example-custom-options/example-custom-options {:mvn/version "0"}}

        :output-dir   "out/cdn"})
