# lein-art Ash Ra Template Leiningen Plugin 



[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/net.vivid-inc/lein-art.svg?color=blue&style=flat-square)](https://clojars.org/net.vivid-inc/lein-art)
[![cljdoc](https://cljdoc.org/badge/net.vivid-inc/lein-art)](https://cljdoc.org/d/net.vivid-inc/lein-art)

`lein-art` is a Leiningen plugin for rendering [Ash Ra Template](https://github.com/vivid-inc/ash-ra-template) `.art` templates.



## Quick Start


```sh
$ cat oracle.art

<( (defn mult [multiplicands] (apply * multiplicands)) )>
Wait, I see it! Your destiny lies deep within the number <(= (mult mysterious-primes) )>.

$ cat project.clj

(defproject rndr "1.2.3"
  :plugins [[net.vivid-inc/lein-art "0.7.2"]]
  :art {:bindings   "{mysterious-primes [7 191]}"
        :templates  "oracle.art"
        :output-dir "."})

$ lein art render
```
`lein-art` will render the output file `oracle` into the current directory.

You can also add `lein-art` to your `~/.lein/profiles.clj`
```clojure
{:user {:plugins [[net.vivid-inc/lein-art "0.7.2"]]}}
```
You'll then be able to render ART templates using `lein` at the CLI anywhere you desire.
```sh
$ lein art --help
```



## Synopsis

`lein-art` can be used with Leiningen `project.clj` and at the CLI.

Templates are supplied as one or more paths to `.art` template files and/or
directory trees thereof.
Those paths are scanned for all ART template files with the `.art`
filename extension.

Templates are rendered and written under `output-dir` stripped of their `.art`
filename extensions, overwriting any existing files with the same paths.
`output-dir` and sub-paths therein are created as necessary.

Specifying options on the CLI will cause ART to form a rendering batch using
those options, ignoring batch definitions in `project.clj`. If no options are
supplied, ART will then expect to find rendering batches in `project.clj`.



#### Options

| `project.clj` | CLI argument | Parameters | Default | Explanation |
| --- | --- | --- | --- | --- |
| `:bindings` | `--bindings` | VAL | | Bindings made available to templates for symbol resolution |
| `:delimiters` | `--delimiters` | VAL | `lispy` | Template delimiters |
| `:dependencies` | `--dependencies` | VAL | | Clojure deps map providing libs within the template evaluation environment. |
| | `-h`, `--help` | | | Displays lovely help and then exits |
| `:output-dir` | `--output-dir` | DIR | `.` | Write rendered files to DIR |
| `:templates` | [FILES] | VAL | | Paths to ART template files |
| `:to-phase` | `--to-phase` | One of: `parse`, `translate`, `enscript`, `evaluate` | `evaluate` | Stop the render dataflow on each template at an earlier phase |
| `--watch-timeout-ms` | VAL | `500` | Trigger re-render once this timeout in milliseconds elapses, coalescing flurries of change to watched batches |

Depending on what types of values a particular option accepts and whether `lein-art` was invoked as a Leiningen configuration or from the CLI,
ART attempts to interpret arguments in this order of precedence:
1. As a map.
1. As the (un-)qualified name of a var.
1. As a path to an EDN file.
1. As a path to a JSON file.
1. As an EDN literal.

**Limitations:** When running as a render batch defined by CLI arguments and not rendering batches in a project file, there is no project in the running context and therefore bindings cannot refer to values that depend on paths within the source code of the immediate project.



## Cookbook



### Custom bindings, delimiters, dependencies, and project code
NOTE: THIS deps.edn EXAMPLE IS INCOMPLETE.
The authors so far don't know how to specify/notate within `project.clj` a Clojure Var that is defined in the project sources.
For the sake of completeness, its value is copy & pasted into the example below in place of the var.
```clojure
; Render all .art template files in the content/ directory to out/cdn/
(defproject art-example-custom-options "0"

  :plugins [[net.vivid-inc/lein-art "0.7.2"]]

  ; Render all .art template files in the content/ directory to out/cdn/
  :art {:templates    "content"

        :bindings     [{manufacturer     "Acme Corporation"    ; Map literal
                        manufacture-year "2022"}

                       ; (See note above)
                       ;#'com.acme.data/product-data                 ; Var, value is a map
                       {products [{:name               "Bag of bird seed"
                                    :weight-kgs         1.0
                                    :minimum-order-qty  50
                                    :unit-price-dollars 0.39M}
                                   {:name               "Ironing board on rollerskates"
                                    :weight-kgs         2.0
                                    :minimum-order-qty  10
                                    :unit-price-dollars 17.95M}]}

                       "{current-year 2021}"                   ; EDN as a string
                       "data/sales-offices.edn"                ; EDN file; top-level form is a map
                       "data/partner-list.json"]               ; JSON file; file content is made available under the symbol 'partner-list


        :delimiters   "jinja"                                  ; Resolves to #'vivid.art.delimiters/jinja

        :dependencies {hiccup/hiccup {:mvn/version "1.0.5"}
                       ; Give templates use of project code.
                       art-example-custom-options {:mvn/version "LATEST"
                                               :local/root "."}}

        :output-dir   "out/cdn"})
```
Install the project code as a Jar into your local `.m2` repository and then
render the ART templates, perhaps as a Leiningen alias:
```sh
$ lein do clean, install, art
...
Rendering ART catalog/index.html
$ diff -r expected/ out/cdn/
```

__Discussion:__
Template syntax is set by the `:delimiters` options.
Clojure forms within the templates can resolve vars and dependencies provided
by several factors: `:bindings` for resolving vars, `:dependencies` for
libraries, and code in the project.

__See also:__
[Example](../examples/custom-options).
[Rendering and options](../art/README.md#rendering-and-options) in the ART documentation.



### Re-render templates whenever they change in a Leiningen project
```
$ cat project.clj
(defproject art-example--watch "0"

  ; ART template batch configuration
  :art {:templates    "resources"
        :output-dir   "target"}

  :plugins [[net.vivid-inc/lein-art "0.7.2"]])  ; Render ART templates with lein-art

$ lein art watch
Press CTRL-C to interrupt watch
Rendering ART resources/...
...
Watching resources/...
```

__Discussion:__
With the CLI command `watch`, ART will monitor `.art` template files in all batches.
Whenever such a file changes, ART will re-render it.
`watch` is especially useful when you are doing development work.

__See also:__
[Example](../examples/watch).



### Configure multi-batch rendering in project.clj
```clojure
  ; Add the lein-art Leiningen plugin:
  :plugins [[net.vivid-inc/lein-art "0.7.2"]]

  ; ART render batches are defined here:
  :art [
    ; An ART render batch configuration
    {:templates    "src/templates/css"
     ; :dependencies use Clojure deps notation and not Leiningen's
     :dependencies {garden/garden {:mvn/version "1.3.10"}}
     :output-dir   "src/resources"}

    ; Another, different batch
    {:templates  ["src/templates/java"]
     :bindings   {version "1.2.3"}
     :output-dir "target/generated-sources/java"}]
```

__Discussion:__
ART render batches can be defined under the top-level key `:art` in `project.clj`,
either as an individual batch or as a collection of any quantity.
The batches will be found and used by `lein-art` when invoked from Leiningen:
```sh
$ lein art render
```


__See also:__
[Example](../examples/multi-batch).



## License

© Copyright 2025 Vivid Inc. and/or its affiliates.
[Apache License 2.0](LICENSE.txt) licensed.
