(defproject art-example--utf-8 "0"

  ; Add the lein-art Leiningen plugin:
  :plugins [[net.vivid-inc/lein-art "0.7.0"]]

  ; Render .art templates
  :art {:templates    "templates"
        :output-dir   "target"
        :bindings     "greek.edn"
        :delimiters   jinja})
