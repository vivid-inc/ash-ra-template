# lein-art Ash Ra Template Leiningen Plugin 



[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/vivid/lein-art.svg?color=blue&style=flat-square)](https://clojars.org/vivid/lein-art)

`lein-art` is a Leiningen plugin for rendering [Ash Ra Template](https://github.com/vivid-inc/ash-ra-template) `.art` templates.



## Quick Start


Templates are supplied as one or more paths to `.art` template files and/or
directory trees thereof.
The `art` Boot task scans those paths for all ART template files with the `.art`
filename extension.
Templates are rendered and written under the output directory `:output-dir`
preserving sub-paths, stripped of the `.art` extension.

```sh
$ cat oracle.art

<% (defn mult [multiplicands] (apply * multiplicands)) %>
Wait, I see it! Your destiny lies deep within the number <%= (mult mysterious-primes) %>.

$ cat project.clj

(defproject ren-da "1.2.3"
  :plugins [[vivid/lein-art "0.5.0"]]
  :art {:bindings   "{mysterious-primes [7 191]}"
        :templates  "oracle.art"
        :output-dir "."})

$ lein art
```
`lein-art` will render the output file `oracle` into the current directory.



## Options

| Keyword | CLI argument | Parameters | Default | Explanation |
| --- | --- | --- | --- | --- |
| `:bindings` | `--bindings` | VAL | | Bindings made available to templates for symbol resolution |
| `:delimiters` | `--delimiters` | VAL | `erb` | Template delimiters |
| `:dependencies` | `--dependencies` | VAL | | Clojure deps map providing libs within the template evaluation environment. Deps maps are merged into this one. Supply your own Clojure dep to override the current version. |
| | `-h`, `--help` | | | Displays lovely help and then exits |
| `:output-dir` | `--output-dir` | DIR | `.` | Write rendered files to DIR |
| `:to-phase` | `--to-phase` | One of: `parse`, `translate`, `enscript`, `evaluate` | `:evaluate` | Stop the render dataflow on each template at an earlier phase |

Options are the keyword equivalent of their corresponding CLI long options.

From the CLI, the `art` Lein task takes a list of file paths to `.art` files (ART templates) and options.
Arguments can be freely mixed.
The same argument can be given multiple times; the effect is additive, merging, or overriding
in nature depending on the option but argument processing order is not guaranteed.
This might be important to you in the event of collisions.

Depending on what types of values a particular option accepts and whether ART is running from within Lein or from a command-line invocation, ART attempts to interpret each argument in the following order:
1. As a map literal.
1. As the (un-)qualified name of a var.
1. As a path to an EDN file.
1. As an EDN literal.

`output-dir` will be created if necessary.
Output files will overwrite files that exist with the same filenames.

CLI arguments can be freely mixed, but are processed in order of appearance which might
be important to you in case of collisions. Depending on what types of values a particular
option accepts and whether ART was invoked from the CLI or as a Leiningen configuration,
ART attempts to interpret each value in this order of precedence:
  1. As a Clojure map literal.
  2. As an (un-)qualified var.
  3. As a string path to an EDN file.
  4. As an EDN literal.

Rendered output files are written to output-dir stripped of their .art filename suffixes, overwriting
any existing files with the same names. output-dir and sub-paths therein are created as necessary.



## Cookbook

`art/test-resources` contains sample Leiningen projects that parallel the automated test suite.


#### Install `lein-art` globally so that you can use it anywhere
Add the plugin to your `~/.lein/profiles.clj`:
```clojure
{:user {:plugins [[vivid/lein-art "0.5.0"]]}}
```


#### Multiple render batches
One or more rendering batches can also be specified as a section in `project.clj':

```clojure
:art [{}
      {}]
```

Run all batches with:
```sh
$ lein art
```



#### WHAT IS THIS

```clojure
  ; Rendered output written to target/index.html
  :art {:templates ["index.html.art"]}

  ; Renders all .art template files in the content/ directory to out/cdn/
  :art {:templates (filter (#.endsWith (.getName %) vivid.art/art-filename-suffix)
                           (file-seq (clojure.java.io/file "content")))

        :bindings     [{:manufacturer     "Acme Inc."          # Map literal
                        :manufacture-year "2019"}
                       com.acme.data/all-data                  # Var, value is a map
                       "data/tabular.edn"]                     # EDN file; top-level form is a map

        :delimiters   vivid.art.delimiters/jinja

        :dependencies {'hiccup {:mvn/version "1.0.5"}
                       'com.acme.core {:mvn/version "1.0.0"    # Use local project from within template code
                                       :local/root  "."}}

        :output       "out/cdn"}
```


## License

© Copyright Vivid Inc.
[Apache License 2.0](LICENSE.txt) licensed.
