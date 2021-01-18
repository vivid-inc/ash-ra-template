(defproject example-custom-options "0"

  :plugins [[vivid/lein-art "0.5.0"]]

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
                       "data/sales-offices.edn"]               ; EDN file; top-level form is a map

        :delimiters   "jinja"                                  ; Resolves to #'vivid.art.delimiters/jinja

        :dependencies {hiccup {:mvn/version "1.0.5"}
                       ; Use local project from within template code.
                       ; Note that Clojure Deps requires a `deps.edn` file in
                       ; order to resolve `com.acme.core` as a dependency.
                       ;com.acme.core {:local/root "."}
                       }

        :output-dir   "out/cdn"})
