# Ash Ra Template Changelog

## [0.6.0]
_Unreleased_
- Breaking change: Bindings can be specified as paths to EDN or JSON files. In such cases, the content of the given bindings file is set as the value of a symbol, created by removing the file extension from the base filename (`.edn`, `.json`).
- Breaking change: With the goal of reducing `vivid/art` project dependencies, branched off CLI-specific code from `vivid/art` into a new `vivid/art-cli` lib. The `:dependencies` option to `vivid.art/render` is also migrated there.
- Breaking change: The default delimiters has been changed to a new `vivid.art.delimiters/lispy` set of delimiters that looks like `<( )>` (note how they ooze with LISP-iness); changed from the prior default of `vivid.art.delimiters/erb`.
- Change: Adding Java 17, an LTS release, to the set of Java versions that ART is tested with.
- Change: Upgraded org.clojure/tools.cli from version 1.0.196 to the new version 1.0.206 which offers the `:multi` option, allowing >= 2 binding specifications on the CLI.
- New: `art-cli`-based tooling allows callers to specify a path to a JSON file to provide bindings.

## [0.5.0]
Released 2021-01-19.
- Breaking change: Renamed `vivid/ash-ra-template` to `vivid/art` to more closely mirror its Clojure namespace.
- License changed from EPL to Apache License, version 2.0.
- Introducing Clojure CLI tools plugin `clj-art`.
- Defect fix: Honors the Java system property `user.dir` when determining file paths.
  The defect came to light during a Maven multi-module build using (`vivid:clojure-maven-plugin`)[https://github.com/vivid-inc/clojure-maven-plugin].
- _(Unstable until version 1.0)_ Render option `:to-phase` allows the dataflow to stop at an earlier `(render)` phase.
  Useful for inspecting ART output at intermediate phases for diagnosis and for understanding ART's behavior.
- Cookbook recipes in README files and (example projects)[../examples/].

## [0.4.0]
Released 2019-07-05.
- Changed template parsing mechanism from regular expressions to instaparse.
- Changed `(render)` from accepting options as keyword arguments to an optional options map.
- Bindings can be supplied with the `(render {:bindings {...})` option.
- Template delimiter definitions can be changed from the default ERB-style with the `(render {:delimiters {...}})` option.
- `vivid.art.delimiters` offers a selection of pre-defined delimiter sets: `erb`, `jinja`, `mustache`, and `php`.
- Introducing Leiningen plugin `lein-art`.
- Both the Boot task and Leiningen plugin are at feature parity, supporting all of `(render)`'s options.

## [0.3.0]
Released 2019-05-03.
- API breaking change: Simplified the renderer namespace from `vivid.art.core/render` to `vivid.art/render`.
- Re-organizing the project into two sub-projects: `ash-ra-template` the library, and a Boot plugin `boot-art`.
- Defining the initial evaluation environment of ART template code as `user`.

## [0.2.0]
Released 2019-04-13.
- Addition of automated tests.
- Switch from `eval-soup` to an embedded evaluation environment based on ShimDandy.

## [0.1.0]
Released 2017-11-20.
- Use `eval-soup` to render templates featuring Clojure language processing with ERB 2.0-esque syntax.

[0.6.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.5.0...ash-ra-template-0.6.0
[0.5.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.4.0...ash-ra-template-0.5.0
[0.4.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.3.0...ash-ra-template-0.4.0
[0.3.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.2.0...ash-ra-template-0.3.0
[0.2.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.1.0...ash-ra-template-0.2.0
[0.1.0]: https://github.com/vivid-inc/ash-ra-template/tree/ash-ra-template-0.1.0
