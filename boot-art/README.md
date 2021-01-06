# boot-art Ash Ra Template Boot Task

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/vivid/boot-art.svg?color=blue&style=flat-square)](https://clojars.org/vivid/boot-art)

`boor-art` is a Boot task for rendering [Ash Ra Template](https://github.com/vivid-inc/ash-ra-template) `.art` templates.



## Usage

The `art` Boot task will render all template files bearing the `.art` filename extension.
The `art` filename extension is stripped from the rendered output filenames.
For example, `index.html.art` is rendered to the file `index.html`.

```clojure
(set-env! :dependencies '[[vivid/boot-art "0.5.0"]])

(require '[vivid.art.boot :refer [art]])

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
  $ boot [earlier tasks ..] -- art [options] -- [later tasks ..]
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
```



## Development

Run the tests with

```bash
boot test
```



## License

© Copyright Vivid Inc.
[Apache License 2.0](LICENSE.txt) licensed.
