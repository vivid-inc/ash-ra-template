# Interesting CLI commands

```bash
# Generate project build files, documentation
$ bin/gen.sh

# Run all tests on the current JVM provided by the environment
$ bin/test.sh

# After the release criteria (see QUALITY.md) are satisfied, deploy a new release
$ bin/deploy.sh

# Examine the full list of transitive dependencies
$ cd $MODULE && lein with-profile '' deps :tree

# Update dependency's clj-kondo configurations:
$ cd $MODULE && lein clj-kondo --copy-configs --dependencies --lint "$(lein classpath)"
```



# Along the path to ART version 1.0 and beyond

### Next:
- Consider how to watch for changes in dependent templates, CLJ source files, anything else.
  Also, when rendering out files, use comparisons to indicate when contents haven't changed, and atomic moves to give other tooling a chance to correctly detect changes and respond properly.
- Heavy testing of quote nesting and escaping, delimiter escaping, Clojure reader forms, comments.
- Move away from `(slurp)` in the libraries. Test to demonstrate how `slurp` can trip up the user. See https://clojuredocs.org/clojure.java.io/resource
- Set the default command for `(dispatch-command)` to `render`, same for all CLI tooling.
- Round out CLI tooling
  - clj-art :exec-fn, fully support `(dispatch-command)`. See https://practical.li/blog-staging/posts/clojure-cli-tools-understanding-aliases/
- Investigate OpenSSF Best Practices reporting, such as: https://bestpractices.coreinfrastructure.org/en/projects/2095
- Implement `(vivid.art/render)` `:classpath` and `:repositories` options.
- `(defmethod)` mechanism for adding options to `(vivid.art/render)`.
- Clarify ^:public-api and ^:internal-api + docstrings. Is there cljdoc precedent?

### Considerations, further out:
- Make `clj-art` and `lein-art` friendly for diagnosing configuration problems, like figwheel.
- Sufficient error reporting.
  Investigate employing an editor backend like Sjacket to track input metadata like line:char positions.
  https://github.com/cgrand/sjacket
- Rework documentation to better accommodate developers browsing github and cljdoc.
  - Project overviews.
  - API documentation.
  - CLI tool usage.
  - Task-specific articles.
  - See https://github.com/cljdoc/cljdoc/blob/master/doc/userguide/for-library-authors.adoc#git-sources
  - See https://github.com/cljdoc/cljdoc-analyzer
- Infer sensible defaults that can be customized via overrides.
- Provide access to more of the execution context from within the evaluation environment: (render) args. The evaluation stack starting from the page through to the current (yield).
- ClojureScript. `art` module only.
- Declare version 1.0.0 once the community deems the ART feature-complete, reliable, and properly documented.
- How to achieve fast runtime performance, fast development & testing feedback loop. Benchmarks with hyperfine.
- Build: Sign releases.
- CLI: Option to re-render templates only when newer than their output files.
- CLI: Ability to list rendered file paths without writing à la `--dry-run`
- Explain the value of ART. Compare and contrast with other templating systems. Emphasize symbolic computation, and the importance of providing native idioms at each point along the value chain, for example a web-based production workflow where professionals handle HTML and CSS.
- Java policies, to give a feasible margin of safety for executing untrusted / unknown code within templates.
- Parsing option mode magic within template content. Example from Jinja: `#jinja2:variable_start_string:'[%', variable_end_string:'%]', trim_blocks: False`
- IDE support for .art files: Eclipse, Emacs, IntelliJ, Vim, VS Code
  - Example IntelliJ plugin project [clj-extras-plugin](https://github.com/brcosta/clj-extras-plugin)
- Maven plugin for rendering ART templates.
- Template registry + Cache à la https://github.com/davidsantiago/stencil , https://github.com/Flamefork/fleet
- AOT compilation.
- Provide ability to compile the input template, perhaps re-writing `(render)` as a macro, or adding a `:compile` render option.
  - Benefits: Useful when the same template is run many times, such as a webserver rendering responses based on a template.
- Container image to run ART from your present CLI.
- The purpose of ART is multi-fold: An ideal substrate for building a custom templating solution such as the constrained Jinja or something more flexible, and as a fully-featured templating system in its own right.



# Bookkeeping
This section records platform-related technological decisions.

**Clojure**:
- Lower-bound of Clojure 1.9.0 for [spec](https://clojure.org/guides/spec).
- Lower-bound of Clojure 1.10.0, farolero's minimum supported version.

**Java**:
- Lower-bound of Java 8, because it strikes a good balance between wide adoption and long-term stability.
- Java LTS releases, as these represent a somewhat stable target with wide adoption.

**Leiningen** is the primary build tool.
- Lower-bound of Leiningen 2.10.0. This is the most recent version of Leiningen provided by CircleCI at the time of this writing.

_Note_: All supported versions (resulting from these facts) are recorded in [assets/vivid-art-facts.edn](assets/vivid-art-facts.edn), used to generate project files, control testing, etc.
