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
  {:art {:extra-deps {vivid/clj-art {:mvn/version "0.5.0"}}
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

`clj-art` is usable with `deps.edn`.

Templates are supplied as one or more paths to `.art` template files and/or
directory trees thereof.
The `art` Boot task scans those paths for all ART template files with the `.art`
filename extension.

Templates are rendered and written under `output-dir` stripped of their `.art`
filename extensions, overwriting any existing files with the same paths.
`output-dir` and sub-paths therein are created as necessary.



#### Options

| Argument | Parameters | Default | Explanation |
| --- | --- | --- | --- |
| `--bindings` | VAL | | Bindings made available to templates for symbol resolution |
| `--delimiters` | VAL | `erb` | Template delimiters |
| `--dependencies` | VAL | | Clojure deps map providing libs within the template evaluation environment. Deps maps are merged into this one. Supply your own Clojure dep to override the current version. |
| `-h`, `--help` | | | Displays lovely help and then exits |
| `--output-dir` | DIR | `.` | Write rendered files to DIR |
| `--to-phase` | One of: `parse`, `translate`, `enscript`, `evaluate` | `evaluate` | Stop the render dataflow on each template at an earlier phase |

Depending on what types of values a particular option accepts,
ART attempts to interpret argument values in this order of precedence:
1. As a map.
1. As the (un-)qualified name of a var.
1. As a path to an EDN file.
1. As an EDN literal.



## Cookbook



#### Override bundled Clojure version
As an implicit dependency, the template execution environment provides ART's minimum supported version of Clojure, version 1.9.0, but this can be overridden by supplying the `org.clojure/clojure` dependency with a different version:
```edn
{:aliases
  {:art {:extra-deps {vivid/clj-art {:mvn/version "0.5.0"}}
         :main-opts  ["-m" "vivid.art.clj-tool" "template.art"
                      "--dependencies" "{org.clojure/clojure,{:mvn/version,\"1.10.1\"}}"]}}}
```
For more information, see [dependencies](../art/README.md#external-dependencies) in ART's documentation.

#### Custom bindings, delims, deps

#### Re-render with watch

#### You can specify several ART rendering batches in `deps.edn', each with a unique alias.

#### Use space characters in arguments within `deps.edn`

When supplying double-quoted parameters to options in your `deps.edn` file, spaces must be replaced with comma ',' characters.
Example:
```edn
  "--dependencies" "{vivid/art {:mvn/version \"0.5.0\"}}"    ; Bad, will fail

  "--dependencies" "{vivid/art,{:mvn/version,\"0.5.0\"}}"    ; OK
```
This mangling is idiosyncratic to `deps.edn`.
`clj-art` invoked at the command line obediently accepts the plain form:
```
$ clojure -m vivid.art.clj-tool \
    --dependencies "{vivid/art {:mvn/version \"0.5.0\"}}"    ; OK
    ...
```



## License

© Copyright Vivid Inc.
[Apache License 2.0](LICENSE.txt) licensed.
