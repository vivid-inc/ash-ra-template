(defproject art-sample--simple "0"

  ; Add the lein-art Leiningen plugin:
  :plugins [[vivid/lein-art "0.6.0"]]

  ; Render .art templates
  :art {:templates    "templates"
        :output-dir   "target"})
