(defproject art-sample--inclusion "0"

  ; Add the lein-art Leiningen plugin:
  :plugins [[net.vivid-inc/lein-art "0.7.0"]]

  ; Render .art templates
  :art {:templates    "templates"
        :bindings     {pace :slow}
        :output-dir   "target"})
