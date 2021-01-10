# Ash Ra Template Changelog

## [0.5.0]
_Unreleased_
- Breaking change: Renamed `vivid/ash-ra-template` to `vivid/art` to more closely mirror its Clojure namespace.
- License changed from EPL to Apache License, version 2.0.
- Introducing Clojure CLI tools plugin `clj-art`.
- Defect fix: Honors the Java system property `user.dir` when determining file paths.
  The defect came to light during a Maven multi-module build using vivid:clojure-maven-plugin.
- _(Unstable until version 1.0)_ Render option `:to-phase` allows the dataflow to stop at an earlier `(render)` phase.
  Useful for inspecting ART output at intermediate phases for diagnosis and for understanding ART's behavior.

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

[0.5.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.4.0...ash-ra-template-0.5.0
[0.4.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.3.0...ash-ra-template-0.4.0
[0.3.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.2.0...ash-ra-template-0.3.0
[0.2.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.1.0...ash-ra-template-0.2.0
[0.1.0]: https://github.com/vivid-inc/ash-ra-template/tree/ash-ra-template-0.1.0
