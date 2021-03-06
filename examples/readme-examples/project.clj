(defproject art-sample--readme-examples "0"

  ; Add the lein-art Leiningen plugin:
  :plugins [[vivid/lein-art "0.5.0"]]

  ; Render .art templates
  :art {:bindings   {mysterious-primes [7 191]}
        :delimiters {:begin-forms "{%" :end-forms "%}" :begin-eval "{%=" :end-eval "%}"}
        :templates  "templates/oracle.art"
        :output-dir "target"})
