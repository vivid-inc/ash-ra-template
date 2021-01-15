(defproject art-sample--utf-8 "0"

  ; Add the lein-art Leiningen plugin:
  :plugins [[vivid/lein-art "0.5.0"]]

  ; Render .art templates
  :art {:templates    "templates"
        :output-dir   "target"
        :bindings     "greek.edn"
        :delimiters   jinja})
