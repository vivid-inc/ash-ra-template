# clj-art Ash Ra Template Clojure Tool 



[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/vivid/clj-art.svg?color=blue&style=flat-square)](https://clojars.org/vivid/clj-art)

`clj-art` is a Clojure `deps.edn` tool for rendering [Ash Ra Template](https://github.com/vivid-inc/ash-ra-template) `.art` templates.

Provided file or directory tree paths containing Ash Ra .art template files and an output dir, this
Clojure tool renders the ART templates to the output dir, preserving relative sub-paths.


## Quick Start

Create an alias in your project `deps.edn` or personal `~/.clojure/deps.edn` map:
```edn
{:aliases 
  {:art {:extra-deps {vivid/clj-art {:mvn/version "0.5.0"}}
         :main-opts  ["-m" "vivid.art.clj-tool"
                      ... ART options ... ART templates]}
```

Templates are supplied as one or more paths to `.art` template files and/or directory trees thereof.

```sh
$ clj -A:art --help
```


### Options

| CLI argument | Parameters | Cardinality | Default | Explanation |
| --- | --- | --- | --- | --- |
| `--bindings` | PARAM | Single or collection | | Bindings made available to templates for symbol resolution |
| `--delimiters` | PARAM | Single or collection | `erb` | Template delimiters |
| ``--dependencies` | PARAM | Single | | Clojure deps map providing libs within the template evaluation environment. Deps maps are merged into this one. Supply your own Clojure dep to override the current version. |
| `-h`, `--help` | | | | Displays lovely help and then exits |
| `--output-dir` | File path | Single | `.` | Write rendered files to DIR |
| `--to-phase` | One of: `parse`, `translate`, `enscript`, `evaluate` | Single | | Stop the render dataflow on each template at an earlier phase |


## Cookbook

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
{:aliases {:art {:extra-dependencies {vivid/clj-art {:mvn/version "0.5.0"}}}}}
```
And use it like this:
```bash
$ clojure -A:art [OPTIONS] [TEMPLATE-PATHS]
```



TODO List at https://github.com/clojure/tools.deps.alpha/wiki/Tools


## License

© Copyright Vivid Inc.
[Apache License 2.0](LICENSE.txt) licensed.