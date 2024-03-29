# lein-art Ash Ra Template Leiningen Plugin 



[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/net.vivid-inc/lein-art.svg?color=blue&style=flat-square)](https://clojars.org/net.vivid-inc/lein-art)

`lein-art` is a Leiningen plugin for rendering [Ash Ra Template](https://github.com/vivid-inc/ash-ra-template) `.art` templates.



## Quick Start


```sh
$ cat oracle.art

<( (defn mult [multiplicands] (apply * multiplicands)) )>
Wait, I see it! Your destiny lies deep within the number <(= (mult mysterious-primes) )>.

$ cat project.clj

(defproject rndr "1.2.3"
  :plugins [[net.vivid-inc/lein-art "0.7.0"]]
  :art {:bindings   "{mysterious-primes [7 191]}"
        :templates  "oracle.art"
        :output-dir "."})

$ lein art
```
`lein-art` will render the output file `oracle` into the current directory.

You can also add `lein-art` to your `~/.lein/profiles.clj`
```clojure
{:user {:plugins [[net.vivid-inc/lein-art "0.7.0"]]}}
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

Depending on what types of values a particular option accepts and whether `lein-art` was invoked as a Leiningen configuration or from the CLI,
ART attempts to interpret arguments in this order of precedence:
1. As a map.
1. As the (un-)qualified name of a var.
1. As a path to an EDN file.
1. As a path to a JSON file.
1. As an EDN literal.



## Cookbook





### Custom bindings, delimiters, dependencies, and project code
NOTE: THIS deps.edn EXAMPLE IS INCOMPLETE
The authors so far don't know how to specify a Var that is defined within `src/`.
For the sake of completeness, its value is copy & pasted into the example below in place of the var.
```clojure
; Render all .art template files in the content/ directory to out/cdn/
(defproject art-example-custom-options "0"

  :plugins [[net.vivid-inc/lein-art "0.7.0"]]

  ; Render all .art template files in the content/ directory to out/cdn/
  :art {:templates    "content"

        :bindings     [{manufacturer     "Acme Corporation"    ; Map literal
                        manufacture-year "2022"}

                       ; (See note above)
                       ;#'com.acme.data/widget                 ; Var, value is a map
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



### Re-render templates whenever their source files change
```clojure
(defproject art-example--watch "0"

  :aliases {"watch" ["auto" "art"]}

  ; ART template batch configuration
  :art {:templates    "resources"
        :output-dir   "target"}

  ; lein-auto configuration
  :auto {:default {:file-pattern #"\.(art)$"    ; Monitor .art files for changes ..
                   :paths ["resources"]}}       ; .. in the resources/ directory

  :plugins [[net.vivid-inc/lein-art "0.7.0"]    ; Render ART templates with lein-art
            [lein-auto "0.1.3"]])       ; Monitor files for changes, run a command on change
```

__Discussion:__
There are several Leiningen plugins that can be used to monitor files and react to changes.
In this example, we use WeaveJester's [`lein-auto`](https://github.com/weavejester/lein-auto).

__See also:__
[Example](../examples/watch).
[Filesystem Watchers](https://www.clojure-toolbox.com/) category at Clojure Toolbox.



### Configure multi-batch rendering in project.clj
```clojure
  ; Two ART render batches are defined here:
  :art [
    ; An ART render batch configuration
    {:templates    "src/templates/css"
     :dependencies {garden/garden {:mvn/version "1.3.10"}}
     :output-dir   "src/resources"}

    ; Another, different batch
    {:templates  ["src/templates/java"]
     :bindings   {version "1.2.3"}
     :output-dir "target/generated-sources/java"}]
```

__Discussion:__
Several ART render batches can be specified as a section in `project.clj':

__See also:__
[Example](../examples/multi-batch).



## License

© Copyright Vivid Inc.
[Apache License 2.0](LICENSE.txt) licensed.
