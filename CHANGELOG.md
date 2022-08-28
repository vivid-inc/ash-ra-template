# Ash Ra Template Changelog

## [0.7.0]
_Unreleased_
### Added
- CLI tools `clj-art` and `lein-art` provide their classpaths to the ART template evaluation environment,
  as well as a reworked `:dependencies` mechanism that uses pomegranate to add further dependencies.
  This deprecates the prior ShimDandy.
- Support for Clojure versions 1.11.0, 1.11.1.
- ART commands:
  - `auto`:   Watches the template files and directories, re-rending on changes.
  - `config`: Dump the effective ART configuration to stdout.
  - `help`:   Display tool help.
  - `render`: ART's default behavior of rendering templates.
- Parallel rendering test suite `vivid.art.parallel-test`. Demonstrates parallelistic use of `(render)` using `(pmap)` as well as core.async `(go)` and `(thread)`.
### Changed
- Template `(emit)` function accepts any number of args, emitting each in order of appearance to the output.
- `:dependencies` now expects a Leiningen-style dependencies map.
- `(vivid.art/render)` function signature now accepts options as keyword arguments. In practice, you only need to replace the map with its contents (delete the curly braces surrounding the map contents).
- `art-cli` honors symlinks.
### Removed
- ShimDandy -based `:dependencies` behavior.
- `boot-art`.

## [0.6.1]
Released 2022-02-25.
### Changed
- Pretty-prints error details as a Clojure data structure. Good starting point for improving on error diagnoses 
  messaging.
### Fixed
- CLI tool didn't catch signals and print error messages properly.

## [0.6.0]
Released 2022-02-24.
### Added
- `art-cli`-based tooling allows callers to specify a path to a JSON file to provide bindings.
- [Ring server](examples/ring-server/) cookbook recipe.
### Changed
- In alignment with [clojars.org verified group names](https://github.com/clojars/clojars-web/wiki/Verified-Group-Names)
  the Maven Group name `vivid` that had served as the umbrella for the ART project changes in this 0.6.0 release to 
  `net.vivid-inc`.
  Please update your dependencies from i.e. `vivid/art` to `net.vivid-inc/art` for ART 0.6.0 onwards.
- Bindings can be specified as paths to EDN or JSON files. In such cases, the content of the given bindings file is set 
  as the value of a symbol, created by removing the file extension from the base filename (`.edn`, `.json`).
- The default delimiters has been changed to a new `vivid.art.delimiters/lispy` that looks like `<( )>` (note how they 
  ooze with LISP-iness); changed from the prior default of `vivid.art.delimiters/erb`.
- Minimum supported Clojure version is advanced from 1.9.0 to 1.10.0 (minimum version supported by farolero).
- With the goal of reducing `net.vivid-inc/art` project dependencies, branched off CLI-specific code from 
  `net.vivid-inc/art` into a new `net.vivid-inc/art-cli` lib. The `:dependencies` option to `vivid.art/render` is also 
  migrated there.
- Adding Java 17, an LTS release, to the set of Java versions that ART is tested with.
- [farolero](https://github.com/IGJoshua/farolero) replaces `special` for condition handling.
- Upgraded org.clojure/tools.cli from version 1.0.196 to the new version 1.0.206 which offers the `:multi` option, 
  allowing >= 2 binding specifications on the CLI.

## [0.5.0]
Released 2021-01-19.
### Added
- Introducing Clojure CLI tools plugin `clj-art`.
- Cookbook recipes in README files and [example projects](examples/).
- _(Unstable until version 1.0)_ Render option `:to-phase` allows the dataflow to stop at an earlier `(render)` phase.
  Useful for inspecting ART output at intermediate phases for diagnosis and for understanding ART's behavior.
### Changed
- Renamed `vivid/ash-ra-template` to `vivid/art` to more closely mirror its Clojure namespace.
- License changed from EPL to Apache License, version 2.0.
### Fixed
- Honors the Java system property `user.dir` when determining file paths.
  The defect came to light during a Maven multi-module build using 
  [`vivid:clojure-maven-plugin`](https://github.com/vivid-inc/clojure-maven-plugin).

## [0.4.0]
Released 2019-07-05.
### Added
- Bindings can be supplied with the `(render {:bindings {...})` option.
- Template delimiter definitions can be changed from the default ERB-style with the `(render {:delimiters {...}})` 
  option.
- `vivid.art.delimiters` offers a selection of pre-defined delimiter sets: `erb`, `jinja`, `mustache`, and `php`.
- Introducing Leiningen plugin `lein-art`.
- Both the Boot task and Leiningen plugin are at feature parity, supporting all of `(render)`'s options.
### Changed
- Changed template parsing mechanism from regular expressions to instaparse.
- Changed `(render)` from accepting options as keyword arguments to an optional options map.

## [0.3.0]
Released 2019-05-03.
### Added
- Defining the initial evaluation environment of ART template code as `user`.
### Changed
- Simplified the renderer namespace from `vivid.art.core/render` to `vivid.art/render`.
- Re-organizing the project into two sub-projects: `ash-ra-template` the library, and a Boot plugin `boot-art`.

## [0.2.0]
Released 2019-04-13.
### Added
- Addition of automated tests.
### Changed
- Switch from `eval-soup` to an embedded evaluation environment based on ShimDandy.

## [0.1.0]
Released 2017-11-20.
### Added
- Use `eval-soup` to render templates featuring Clojure language processing with ERB 2.0-esque syntax.

[0.7.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.6.1...ash-ra-template-0.7.0
[0.6.1]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.6.0...ash-ra-template-0.6.1
[0.6.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.5.0...ash-ra-template-0.6.0
[0.5.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.4.0...ash-ra-template-0.5.0
[0.4.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.3.0...ash-ra-template-0.4.0
[0.3.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.2.0...ash-ra-template-0.3.0
[0.2.0]: https://github.com/vivid-inc/ash-ra-template/compare/ash-ra-template-0.1.0...ash-ra-template-0.2.0
[0.1.0]: https://github.com/vivid-inc/ash-ra-template/tree/ash-ra-template-0.1.0
