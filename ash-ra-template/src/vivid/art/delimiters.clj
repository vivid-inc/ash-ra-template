; Copyright 2019 Vivid Inc.

(ns vivid.art.delimiters
  "Definitions of template delimiter tags in the style of Ruby ERB
  and various other templating systems.")

; Referencing https://github.com/ruby/ruby/blob/trunk/lib/erb.rb
(def ^:const erb {:begin-forms "<%"
                  :end-forms   "%>"
                  :begin-eval  "<%="})

; Referencing http://jinja.pocoo.org/docs/2.10/templates/
; TODO Jinja is dependent on implementation of the bindings feature.
#_(def ^:const jinja {:begin-forms "{%"
                      :end-forms   "%}"
                      :begin-eval  "{{"
                      :end-eval    "}}"})

; Referencing https://mustache.github.io/mustache.5.html
; TODO Mustache is dependent on implementation of the bindings feature.
#_(def ^:const mustache {:begin-forms "{{"
                         :end-forms   "}}"})

(def ^:const php {:begin-forms "<?"
                  :end-forms   "?>"
                  :begin-eval  "<?="})
