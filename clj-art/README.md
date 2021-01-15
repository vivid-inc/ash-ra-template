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

The same argument can be given multiple times; the effect is additive, merging, or overriding
in nature depending on the option but argument processing order is not guaranteed.
This might be important to you in the event of collisions.

Depending on what types of values a particular option accepts,
ART attempts to interpret argument values in this order of precedence:
1. As a map.
1. As the (un-)qualified name of a var.
1. As a path to an EDN file.
1. As an EDN literal.



## Cookbook

`art/test-resources` contains sample Clojure Tool projects that parallel the automated test suite, including all examples shown here.

#### Override version of Clojure dependency

#### Custom bindings, delims, deps

####

####

####










####
You can specify several ART rendering batches in `deps.edn', each with a unique alias.

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

#### Use `clj-art` globally

Add the following to your `~/.clojure/deps.edn` file:
```edn
{:aliases {:art {:extra-deps {vivid/clj-art {:mvn/version "0.5.0"}}}}}
```
And use it like this:
```bash
$ clojure -A:art [OPTIONS] [TEMPLATE-PATHS]
```



## License

© Copyright Vivid Inc.
[Apache License 2.0](LICENSE.txt) licensed.
