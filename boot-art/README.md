# Ash Ra Template Boot Task

Boot task for processing [Ash Ra Template](https://github.com/vivid-inc/ash-ra-template) `.art` files.

[![Clojars Project](https://img.shields.io/clojars/v/vivid/boot-art.svg)](https://clojars.org/vivid/boot-art)



## Usage

The `art` Boot task will render all template files bearing the `.art` filename suffix.
This suffix is stripped from the rendered output filenames.
For example, `index.html.art` is rendered to the file `index.html`.

```clojure
(set-env! :dependencies '[[vivid/boot-art "0.3.0"]])

(require '[vivid.art.boot :refer [art]])

(deftask pipeline []
  (comp ...
        (art)
        ...))
```



## License

© Copyright Vivid Inc.
[EPL](LICENSE.txt) licensed.
