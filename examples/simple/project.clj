(defproject art-sample--simple "0"

  ; Add the lein-art Leiningen plugin:
  :plugins [[net.vivid-inc/lein-art "0.6.1"]]

  ; Render .art templates
  :art {:templates    "templates"
        :output-dir   "target"})
