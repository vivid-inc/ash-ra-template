# Developing Ash Ra Template

## Interesting CLI commands

```bash
# Generate project build files, documentation
$ bin/gen.sh

# Lint all projects
$ bin/lein-star.sh lint

# Run all tests on the current JVM provided by the environment
$ bin/test.sh

# After the release criteria (see QUALITY.md) are satisfied, deploy a new release
$ bin/deploy.sh

# Examine the full list of transitive dependencies
$ cd $MODULE && lein with-profile '' deps :tree

# Update dependency's clj-kondo configurations:
$ cd $MODULE && lein clj-kondo --copy-configs --dependencies --lint "$(lein classpath)"
```



## Development philosophy

The public API is designed around a functional approach, with as little magic as its authors can manage.
In the automated tests, some values may appear nonsensical or even absurd, but we need to account for all eventualities, including:
- Programmatic manipulation of values, concatenation of strings, etc.
- If it is possible, someone may eventually try it.



## Known defects and limitations
- `:dependencies` in each ART batch linger in their parent classloader, accumulating and leaking with subsequent
  batches. See [art-cli/src/vivid/art/cli/classpath.clj].
- Templates that generate clj functions larger than the 64KB limit fail, due to:
  https://github.com/clojure/clojure/blob/13a2f67b91ab81cd109ea3152fce1ae76d212453/src/jvm/clojure/asm/ByteVector.java#L242C21-L242C28
- In nested rendering, passing a block whose contents when serialized contains Clojure forms will cause Clojure's
  LispReader to attempt to evaluate those forms, potentially resulting in bizarre behavior or failure.



## Along the path to ART version 1.0 and beyond

### Next:
- Allow the batch specification `templates` argument to also accept quoted functions, in addition to stringified file paths. Fuctions will be called with the batch as their first argument and are expected to return a seq of render jobs.
- `watch` command: Tolerate failure on the first full pass. This might be accomplished by first entering watch mode, then queueing a full render.
- CLI option to either fail command by first attempting the entire batch then reporting exit code 
  (default, consistent with `watch`), or at first render error `--fail-fast` (applicable only for `render`, not `watch`).
- `vivid.art.cli.resolve/resolve-as-var` and `*-example-custom-options`
- Ability to specify named batches, and run only those batches in a rendering run.
- Accept a varname as a template path. Use either its return value (if IFn) or the value as a set of template path-specs / rendering batch.
- `(slurp)` defaults to decoding input files as UTF-8; this might trip up template authors. Instead, document this stumbling block, support `java.io.reader` in the protocol, and eliminate use of `slurp` in this repo.
- Heavy testing of quote nesting and escaping, delimiter escaping, Clojure reader forms, comments.
- clj-art :exec-fn, fully support `(dispatch-command)`. See https://practical.li/blog-staging/posts/clojure-cli-tools-understanding-aliases/
- Investigate OpenSSF Best Practices reporting, such as: https://bestpractices.coreinfrastructure.org/en/projects/2095
- Explore a `(defmethod)` mechanism for adding options to `(vivid.art/render)`, and try it with `:classpath` and `:repositories` options.
- Watch: Document how watches trigger re-renders: All sub-paths under the watched dir.
- Watch: When rendering out files, use comparisons to indicate when contents haven't changed, and atomic moves to give other tooling a chance to correctly detect changes and respond properly.
- CLI: Ability to list rendered file paths without writing à la `--dry-run`
- Possible defect: Are :dependencies leaked to subsequent ART render batches, without being expressly mentioned as being dependencies?
- `watch` command: Tolerate failure on the first full pass. This might be accomplished by first entering watch mode, then queueing a full render.
- CLI option to either fail command at first render error or attempt the entire batch then report exit code (default, consistent with `watch`).
- `vivid.art.cli.resolve/resolve-as-var` and `*-example-custom-options`
- Ability to specify named batches, and run only those batches in a rendering run.
- Extend vivid.art/Render protocol to handle Java `Reader` or `InputStream` types. See https://clojuredocs.org/clojure.java.io/reader



### Considerations, further out:
- Delve into details of `(yield)`:
```
; These are equivalent:
<(= (yield :body )> ... default content ... <( ) )>
(defn yields? [symbol'] (when-let [x (resolve symbol')] (var-get x)))
(defn yield-nil [symbol'] (when-let [x (resolve symbol')] (emit (var-get x))))
<( (when (yields? :body) )><(= (yield :body) )> ... default content ... <( ) )>
```
- In `watch` mode, memorize which files are written. When the source file changes its name, keep the output better in sync by deleting the affected rendered output file.
  This can be accomplished by conveniently viewing a rename operation as two coordinated operations:
  - When template is deleted, delete its rendered output.
  - When new template file is added and matches the glob, add it to the rendering set.
  - When template matching glob is changed, offer choice of rendering that one file vs. rendering entire batch.
- Make `clj-art` and `lein-art` friendly for diagnosing configuration problems, like figwheel.
- Sufficient error reporting.
  Investigate employing an editor backend like Sjacket to track input metadata like line:char positions.
  https://github.com/cgrand/sjacket
- Rework documentation to better accommodate developers browsing github and cljdoc.
  - Project overviews.
  - API documentation.
  - CLI tool usage.
  - Task-specific articles.
  - Clarify ^:public-api and ^:internal-api + docstrings. Is there cljdoc precedent?
  - See https://github.com/cljdoc/cljdoc/blob/master/doc/userguide/for-library-authors.adoc#git-sources
  - See https://github.com/cljdoc/cljdoc-analyzer
- Infer sensible defaults that can be customized via overrides.
- ClojureScript. `art` module only. Perhaps start by formalizing independence of `art` sources from Java by renaming source files from `.clj` to `.cljc` where possible.
- Declare version 1.0.0 once the community deems the ART feature-complete, reliable, and properly documented.
- How to achieve fast runtime performance, fast development & testing feedback loop. Benchmarks with hyperfine.
- Build: Sign releases.
- Explain the value of ART. Compare and contrast with other templating systems. Emphasize symbolic computation, and the importance of providing native idioms at each point along the value chain, for example a web-based production workflow where professionals handle HTML and CSS.
- Java policies, to give a feasible margin of safety for executing untrusted / unknown code within templates.
- Parsing option mode magic within template content. Example from Jinja: `#jinja2:variable_start_string:'[%', variable_end_string:'%]', trim_blocks: False`
- IDE support for .art files: Eclipse, Emacs, IntelliJ, Vim, VS Code
  - Example IntelliJ plugin project [clj-extras-plugin](https://github.com/brcosta/clj-extras-plugin)
- Maven plugin for rendering ART templates.
- Template registry + Cache à la https://github.com/davidsantiago/stencil , https://github.com/Flamefork/fleet
- AOT compilation.
- Provide ability to compile the input template, perhaps re-writing `(render)` as a macro, or adding a `:compile` render option.
  - Useful when the same template is run many times, such as a webserver rendering responses based on a template.
  - Produces a plain function. `(def page (vivid.art/render-compiled (slurp "index.html.art"))) (page p)`
- Container image to run ART from your present CLI.
- The purpose of ART is multi-fold: An ideal substrate for building a custom templating solution such as the constrained Jinja or something more flexible, and as a fully-featured templating system in its own right.
- babashka, jank-lang.
