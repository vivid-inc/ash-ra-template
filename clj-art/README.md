# clj-art Ash Ra Template Clojure Tool 



[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/vivid/clj-art.svg?color=blue&style=flat-square)](https://clojars.org/vivid/clj-art)

`clj-art` is a Clojure `deps.edn` tool for rendering [Ash Ra Template](https://github.com/vivid-inc/ash-ra-template) `.art` templates.



## Quick Start


```sh
$ cat oracle.art

<% (defn mult [multiplicands] (apply * multiplicands)) %>
Wait, I see it! Your destiny lies deep within the number <%= (mult mysterious-primes) %>.

$ cat deps.edn

{:aliases
  {:art {:extra-deps {vivid/clj-art {:mvn/version "0.6.0"}}
         :main-opts  ["-m" "vivid.art.clj-tool"]}}}

$ clojure -A:art --bindings "{mysterious-primes [7 191]}" \
                 oracle.art
```
`clj-art` will render the output file `oracle` into the current directory.

You can also add the above alias to your personal `~/.clojure/deps.edn`.
You'll then be able to render ART templates using `clojure` at the CLI anywhere you desire.
```sh
$ clojure -A:art --help
```



## Synopsis

`clj-art` is used with `deps.edn`.

Templates are supplied as one or more paths to `.art` template files and/or
directory trees thereof.
Those paths are scanned for all ART template files with the `.art`
filename extension.

Templates are rendered and written under `output-dir` stripped of their `.art`
filename extensions, overwriting any existing files with the same paths.
`output-dir` and sub-paths therein are created as necessary.



#### Options

| Argument | Parameters | Default | Explanation |
| --- | --- | --- | --- |
| `--bindings` | VAL | | Bindings made available to templates for symbol resolution. Currently limited to a single usage in `clj-art`. |
| `--delimiters` | VAL | `erb` | Template delimiters |
| `--dependencies` | VAL | | Clojure deps map providing libs within the template evaluation environment. |
| `-h`, `--help` | | | Displays lovely help and then exits |
| `--output-dir` | DIR | `.` | Write rendered files to DIR |
| `--to-phase` | One of: `parse`, `translate`, `enscript`, `evaluate` | `evaluate` | Stop the render dataflow on each template at an earlier phase |

Depending on what types of values a particular option accepts,
ART attempts to interpret arguments in this order of precedence:
1. As a map.
1. As the (un-)qualified name of a var.
1. As a path to an EDN file.
1. As an EDN literal.



## Cookbook





### Custom bindings, delimiters, dependencies, and project code
```edn
{:aliases
 {:art {:extra-deps {vivid/clj-art {:mvn/version "0.6.0"}}
        :main-opts  ["-m" "vivid.art.clj-tool"

                     ; Render all .art templates in the content/ directory
                     "content"

                     ; Map as a string
                     "--bindings" "{manufacturer,\"Acme,Corporation\",manufacture-year,\"2022\"}"
                     ; Var whose value is a map
                     "--bindings" "com.acme.data/product-data"
                     ; EDN as a string
                     "--bindings" "{current-year,2021}"
                     ; EDN file; top-level form is a map
                     "--bindings" "data/sales-offices.edn"

                     ; Unqualified, resolves to #'vivid.art.delimiters/jinja
                     "--delimiters" "jinja"

                     "--dependencies" "{hiccup,{:mvn/version,\"1.0.5\"}}"
                     "--to-phase" "enscript"
                     ; Render to the our/cdn/ directory
                     "--output-dir" "out/cdn"]}}}
```

__Discussion:__
Template syntax is set by the `:delimiters` options.
Clojure forms within the templates can resolve vars and dependencies provided
by several factors: `:bindings` for resolving vars, `:dependencies` for
libraries, and code in the project.

__See also:__
[Example](../examples/custom-options).
[Rendering and options](../art/README.md#rendering-and-options) in the ART documentation.



### Use space characters in arguments within deps.edn
__Discussion:__
When supplying double-quoted parameters to options in your `deps.edn` file,
spaces must be replaced with comma ',' characters.
```edn
  "--dependencies" "{hiccup {:mvn/version \"1.0.5\"}}"    ; Bad, will fail

  "--dependencies" "{hiccup,{:mvn/version,\"1.0.5\"}}"    ; OK
```
This mangling is idiosyncratic to `deps.edn`.
`clj-art` invoked at the command line obediently accepts the plain form:
```
$ clojure -m vivid.art.clj-tool \
    "--dependencies" "{hiccup {:mvn/version \"1.0.5\"}}"    ; OK
    ...
```

__See also:__
[Example](../examples/all-options).



### Override bundled Clojure version
```edn
{:aliases
  {:art {:extra-deps {vivid/clj-art {:mvn/version "0.6.0"}}
         :main-opts  ["-m" "vivid.art.clj-tool" "templates"
                      "--dependencies" "{org.clojure/clojure,{:mvn/version,\"1.10.3\"}}"]}}}
```

__Discussion:__
As an implicit dependency, the template execution environment provides ART's
minimum supported version of Clojure, version 1.9.0,
but this can be overridden by supplying the `org.clojure/clojure` dependency
with a different version.

__See also:__
[Example](../examples/override-clojure-version).
[`:dependencies`](../art/README.md#external-dependencies) in the ART documentation.



### Configure multi-batch rendering in deps.edn
```edn
{:aliases
  {:rndr-a {:extra-deps {vivid/clj-art {:mvn/version "0.6.0"}}
            :main-opts  ["-m" "vivid.art.clj-tool" "src/templates/css"
                         "--dependencies" "{garden,{:mvn/version,\"1.3.10\"}}"
                         "--output-dir" "src/resources"]}
   :rndr-b {:extra-deps {vivid/clj-art {:mvn/version "0.6.0"}}
            :main-opts  ["-m" "vivid.art.clj-tool" "src/templates/java"
                         "--bindings" "{version,\"1.2.3\"}"
                         "--output-dir" "target/generated-sources/java"]}}}
```

__Discussion:__
Each individual render batch is assigned its own unique key under `:aliases`,
in this example aliases `rndr-a` and `rndr-b`. As `deps.edn` is not a build tool,
but instead focuses on dependency resolution and the running of a single entry point,
we are able to run any one batch:
```bash
$ clojure -A:rndr-a
```

__See also:__
[Example](../examples/multi-batch).



## License

© Copyright Vivid Inc.
[Apache License 2.0](LICENSE.txt) licensed.
