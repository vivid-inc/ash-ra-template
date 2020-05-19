# Ash Ra Template Boot Task [![Clojars Project](https://img.shields.io/clojars/v/vivid/boot-art.svg?color=239922&style=flat-square)](https://clojars.org/vivid/boot-art)

Boot task for rendering [Ash Ra Template](https://github.com/vivid-inc/ash-ra-template) `.art` templates.



## Usage

The `art` Boot task will render all template files bearing the `.art` filename extension.
The `art` filename extension is stripped from the rendered output filenames.
For example, `index.html.art` is rendered to the file `index.html`.

```clojure
(set-env! :dependencies '[[vivid/boot-art "0.5.0"]])

(require '[vivid.art.boot :refer [art]])

(deftask pipeline []
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

Templates are rendered to files whose filenames are stripped of the .art suffix.

Options:
  -h, --help              Print this help info.
  -b, --bindings VAL      VAL sets bindings made available to templates for symbol resolution.
  -d, --delimiters VAL    VAL sets template delimiters (EDN or a Var).
      --dependencies VAL  VAL sets clojure deps map (EDN or a Var).
  -f, --files FILES       FILES sets a vector of .art template files to render. If not present, all files will be rendered.
```



## Development

Run the tests with

```bash
boot test
```



## License

© Copyright Vivid Inc.
[Apache License 2.0](LICENSE.txt) licensed.
