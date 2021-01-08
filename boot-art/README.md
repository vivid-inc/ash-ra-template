# boot-art Ash Ra Template Boot Task

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/vivid/boot-art.svg?color=blue&style=flat-square)](https://clojars.org/vivid/boot-art)

`boot-art` is a [Boot](https://github.com/boot-clj/boot) task for rendering [Ash Ra Template](https://github.com/vivid-inc/ash-ra-template) `.art` templates.



## Usage

The `art` Boot task will render all template files bearing the `.art` filename extension.
The `art` filename extension is stripped from the rendered output filenames.
For example, `index.html.art` is rendered to the file `index.html`.

```clojure
(set-env! :dependencies '[[vivid/boot-art "0.5.0"]])

(require '[vivid.art.boot-task :refer [art]])

(deftask my-pipeline []
  (comp ...
        (art :bindings     VAL
             :delimiters   VAL
             :dependencies VAL
             :files        FILES)
        ...))
```

Command-line usage:

```
  $ boot -d vivid/boot-art art
```

and options:

```clojure
Render Ash Ra .art templates.

Provided one or more template files and any quantity of optional bindings, this
Boot task writes rendered template output to a specified output dir.
Templates are rendered to files whose filenames are stripped of the .art suffix.

Options:
  -h, --help              Print this help info.
  -b, --bindings VAL      VAL sets bindings made available to templates for symbol resolution.
  -d, --delimiters VAL    VAL sets template delimiters (default: `erb').
      --dependencies VAL  VAL sets clojure deps map providing libs within the template evaluation environment.
  -p, --to-phase VAL      VAL sets stop the render dataflow on each template at an earlier phase.
  -f, --files FILES       FILES sets a vector of .art template files to render. If not present, all files will be rendered
  -o, --output-dir DIR    DIR sets write rendered files to DIR. Leave unset to have Boot decide.
```


## Cookbook

#### CLI: Render ART templates with bindings and custom delimiters to a specific directory
```bash
$ cat oracle.art

{% (defn mult [multiplicands] (apply * multiplicands)) %}
Wait, I see it! Your destiny lies deep within the number {%= (mult mysterious-primes) %}.

$ boot art --bindings "'{mysterious-primes [7 191]}" \
           --delimiters "'{:begin-forms \"{%\" :end-forms \"%}\" :begin-eval \"{%=\" :end-eval \"%}\"}" \
           --files oracle.art \
           --output-dir .
```

Discussion: Command-line arguments presented by Boot to the `boot-art` task are interpreted as code.
You can prevent evaluation of undefined symbols by quoting them with a single quote `'` as above.



## Development

Run the tests with

```bash
boot test
```



## License

© Copyright Vivid Inc.
[Apache License 2.0](LICENSE.txt) licensed.
