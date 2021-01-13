# boot-art Ash Ra Template Boot Task



[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/vivid/boot-art.svg?color=blue&style=flat-square)](https://clojars.org/vivid/boot-art)

`boot-art` is a [Boot](https://github.com/boot-clj/boot) task for rendering [Ash Ra Template](https://github.com/vivid-inc/ash-ra-template) `.art` templates.
It composes easily into your existing Boot infrastructure.



## Quick Start

The `art` Boot task will render all template files bearing the `.art` filename extension.
The `art` filename extension is stripped from the rendered output filenames.
For example, `index.html.art` is rendered to the file `index.html`.
In your `build.boot`:

```clojure
(set-env! :dependencies '[[vivid/boot-art "0.5.0"]]
          :source-paths #{"templates"})

(require '[vivid.boot-art :refer [art]])

(deftask my-pipeline []
         (comp ...
               (art :bindings     VAL
                    :delimiters   VAL
                    :dependencies VAL)
               ...
               (target)))
```

Standalone CLI usage:

```
  $ boot -d vivid/boot-art art [OPTIONS]
```

and options:

```clojure
  -h, --help              Print this help info.
      --bindings VAL      VAL sets bindings made available to templates for symbol resolution.
      --delimiters VAL    VAL sets template delimiters (default: `erb').
      --dependencies VAL  VAL sets clojure deps map providing libs within the template evaluation environment.
      --files FILES       FILES sets render these ART files and directory trees thereof, instead of Boot's fileset
      --output-dir DIR    DIR sets divert rendered file output to DIR.
      --to-phase VAL      VAL sets stop the render dataflow on each template at an earlier phase.
```



## Cookbook

`art/test-resources` contains sample Boot projects that parallel the automated test suite.


#### CLI: Render ART templates with bindings and custom delimiters
```bash
$ cat oracle.art

{% (defn mult [multiplicands] (apply * multiplicands)) %}
Wait, I see it! Your destiny lies deep within the number {%= (mult mysterious-primes) %}.

$ boot art --bindings "'{mysterious-primes [7 191]}" \
           --delimiters "'{:begin-forms \"{%\" :end-forms \"%}\" :begin-eval \"{%=\" :end-eval \"%}\"}" \
           --files oracle.art
```

Discussion:
Command-line arguments presented by Boot to the `boot-art` task are interpreted as code.
You can prevent evaluation of undefined symbols by quoting them with a single quote `'`, for example as above.


#### `build.boot`: Specifying ART template files and/or the output directory
```clojure
(import '(java.io File))

(deftask render-art []
         (art :files #{"source/index.html.art" "templates"}
              :output-dir (File. "out")))
```
Discussion:
If `:files` is specified, `art` will use those files instead of searching Boot's fileset.
Providing an `:output-dir` will cause templates to be written there as well as to Boot's `(target)` (if any).


#### `build.boot`: Re-render templates whenever their source files change
```clojure
(set-env! :resource-paths #{"templates"})

(deftask dev []
         (comp (watch)
               (art)
               (target)))
```



## License

© Copyright Vivid Inc.
[Apache License 2.0](LICENSE.txt) licensed.
