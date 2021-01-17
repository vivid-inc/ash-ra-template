(defproject example-custom-options "0"

  :plugins [[vivid/lein-art "0.5.0"]]

  ; Render all .art template files in the content/ directory to out/cdn/
  :art {:templates    "content"

        :bindings     [{'manufacturer     "Acme Corporation"   ; Map literal
                        'manufacture-year "2099"}
                       ;#'com.acme.data/widget                   ; Var, value is a map
                       "{current-year 2021}"                   ; EDN as a string
                       "data/sales-partners.edn"]              ; EDN file; top-level form is a map

        :delimiters   vivid.art.delimiters/jinja                                   ; Resolves to #'vivid.art.delimiters/jinja

        :dependencies {hiccup {:mvn/version "1.0.5"}
                       ; Use local project from within template code.
                       ; Note that Clojure Deps requires a `deps.edn` file in
                       ; order to resolve `com.acme.core` as a dependency.
                                        ;com.acme.core {:local/root "."}
                       }
        :to-phase :enscript

        :output-dir   "out/cdn"})
