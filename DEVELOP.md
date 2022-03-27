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
- `auto` CLI command.
- `print-conf` CLI command dumps the effective configuration after processing project configuration and CLI args.
- `(include)` and `(yield)` -like content inclusion/nesting mechanisms. One simple and one complicated exemplar.
- Heavy testing of quote nesting and escaping, delimiter escaping, Clojure reader forms, comments.
- Sufficient error reporting.

### Considerations, further out:
- Declare version 1.0.0 once the community deems the ART feature-complete, reliable, and properly documented.
- Explicit support for parallel execution.
- Provide access to execution context from within the evaluation environment: (render) args. The evaluation stack starting from the page through to the current (yield). `user/*render-context*`
- Infer sensible defaults that can be customized via overrides.
- ClojureScript.
- How to achieve fast runtime performance, fast development & testing feedback loop.
- Build: Sign releases.
- CLI: Option to re-render templates only when newer than their output files.
- CLI: Ability to list rendered file paths without writing à la `--dry-run`
- Explain the value of ART. Compare and contrast with other templating systems. Emphasize symbolic computation, and the importance of providing native idioms at each point along the value chain, for example a web-based production workflow where professionals handle HTML and CSS.
- Java policies, to make it possible to execute untrusted / unknown code within templates.
- Parsing option mode magic within template content. Example from Jinja: `#jinja2:variable_start_string:'[%', variable_end_string:'%]', trim_blocks: False`
- IDE support for .art files: Eclipse, Emacs, IntelliJ, Vim, VS Code
- Maven plugin for rendering ART templates.
- Template registry + Cache à la https://github.com/davidsantiago/stencil.
- AOT compilation.



# Bookkeeping

## Minimum supported versions
Clojure:
- Lower-bound of Clojure 1.9.0 for [spec](https://clojure.org/guides/spec)
- Lower-bound of Clojure 1.9.0 because it is compatible with a ``clojure.alpha.tools.deps`` version that has reasonable Maven-style dependency resolution capability.
- Lower-bound of Clojure 1.10.0, farolero's minimum supported version.

Java:
- Lower-bound of Java 8, because it strikes a good balance between wide adoption and long-term stability.
