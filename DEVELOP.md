# Commands of interest

```bash
# Generate project build files, documentation
$ bin/gen.sh

# Run all tests on the current JVM provided by the environment
$ bin/test.sh

# After the release criteria (see QUALITY.md) are satisfied, deploy a new release
$ bin/deploy.sh
```



# Along the path to ART version 1.0 and beyond

### Next:
- Consider how to watch for changes in dependent templates, CLJ source files, anything else.
- Heavy testing of quote nesting and escaping, delimiter escaping, Clojure reader forms, comments.
- clj-art :exec-fn. See https://practical.li/blog-staging/posts/clojure-cli-tools-understanding-aliases/
- Change from DCO to a Contributor's License Agreement.
- Investigate OpenSSF Best Practices reporting, such as: https://bestpractices.coreinfrastructure.org/en/projects/2095
- Container image to run ART from your present CLI.

### Considerations, further out:
- Make `clj-art` and `lein-art` friendly for diagnosing configuration problems, like figwheel.
- Declare version 1.0.0 once the community deems the ART feature-complete, reliable, and properly documented.
- Sufficient error reporting.
- Provide access to more of the execution context from within the evaluation environment: (render) args. The evaluation stack starting from the page through to the current (yield).
- Infer sensible defaults that can be customized via overrides.
- ClojureScript.
  - `art` module only.
  - `(render)` function `:bindings` option applicability is Clojure only, the remainder make sense in both CLJ and CLJS worlds.
  - Replace `reduce-fsm` with [metosin/tilakone.core](https://github.com/metosin/tilakone/network) with improvements.
- How to achieve fast runtime performance, fast development & testing feedback loop. Benchmarks with hyperfine.
- Build: Sign releases.
- CLI: Option to re-render templates only when newer than their output files.
- CLI: Ability to list rendered file paths without writing à la `--dry-run`
- Explain the value of ART. Compare and contrast with other templating systems. Emphasize symbolic computation, and the importance of providing native idioms at each point along the value chain, for example a web-based production workflow where professionals handle HTML and CSS.
- Java policies, to make it possible to execute untrusted / unknown code within templates.
- Parsing option mode magic within template content. Example from Jinja: `#jinja2:variable_start_string:'[%', variable_end_string:'%]', trim_blocks: False`
- IDE support for .art files: Eclipse, Emacs, IntelliJ, Vim, VS Code
- Maven plugin for rendering ART templates.
- Template registry + Cache à la https://github.com/davidsantiago/stencil , https://github.com/Flamefork/fleet
- AOT compilation.
- Rewrite (render) as a macro that compiles the input template.
- Accomodate cljdoc.



# Bookkeeping

## Technology choices
Leiningen is the primary build tool.

## Minimum supported versions
Note: All supported versions (resulting from these facts) are recorded in [assets/vivid-art-facts.edn](assets/vivid-art-facts.edn); a file that controls testing.

Clojure:
- Lower-bound of Clojure 1.9.0 for [spec](https://clojure.org/guides/spec)
- Lower-bound of Clojure 1.9.0 because it is compatible with a ``clojure.alpha.tools.deps`` version that has reasonable Maven-style dependency resolution capability.
- Lower-bound of Clojure 1.10.0, farolero's minimum supported version.

Java:
- Lower-bound of Java 8, because it strikes a good balance between wide adoption and long-term stability.

Leiningen:
- Lower-bound of Leiningen 2.9.8. This is the most recent version of Leiningen provided by CircleCI at the time of this writing.
