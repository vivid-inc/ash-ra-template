# Ash Ra Template

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/vivid/art.svg?color=blue&style=flat-square)](https://clojars.org/vivid/art)
[![CircleCI](https://circleci.com/gh/vivid-inc/ash-ra-template/tree/master.svg?style=svg)](https://circleci.com/gh/vivid-inc/ash-ra-template/tree/master)
[![Codecov](https://codecov.io/gh/vivid-inc/ash-ra-template/branch/master/graph/badge.svg)](https://codecov.io/gh/vivid-inc/ash-ra-template)

Expressive & customizable template system featuring Clojure language processing

Tested with Clojure 1.9 and newer, Java 8 and newer LTS releases.



## Components

[ART library](art/README.md), including detailed information about using the ART library and ART template syntax, rendering API and options, and processing.

[ART CLI library](art-cli/README.md) aggregates code common to the translation and processing of ART build tool and command line arguments into parameters for ART's Clojure API.

[Boot task](boot-art/README.md) for rendering ART templates.

[Clojure tool](clj-art/README.md) for rendering ART templates.

[Leiningen plugin](lein-art/README.md) for rendering ART templates.



<a name="quickstart"></a>
## Quick Start

Include this library from Clojars by adding the latest version of ``vivid/art`` to your project dependencies:
```clojure
(set-env! :dependencies '[[vivid/art "0.6.0"]])    ; Boot build.boot
{:deps {vivid/art {:mvn/version "0.6.0"}}}         ; Clojure tools deps.edn
:dependencies [[vivid/art "0.6.0"]]                ; Leiningen project.clj
```

Render a template string:
```clojure
(require '[vivid.art :as art])

(art/render "There were <(= (+ 1 2) )> swallows, dancing in the sky.")
```

Or, to render from a file:
```clojure
(art/render (slurp "index.html.art"))
```

#### Contra-indicators
Can your intended application of ART withstand the following?
- Impoverished error reporting. You thought Clojure was bad?
- Coincidental appearance of Clojure reader symbols in template may give rise to unexpected behavior.


## Contributing

**Pull Requests** are welcome!
We work with people offering PRs to revise and iterate leading to solutions in accord with project goals and [release criteria](QUALITY.md).
Commits must include Signed-off-by indicating acceptance of the [Developer's Certificate of Origin](DCO.txt).
Unproductive behavior such as unkindness towards others and derailment is not tolerated.

### Along the Path to Version 1.0 and Beyond

#### Next:
- Replace special with https://github.com/IGJoshua/farolero for conditions and restarts.
- `(include)` and `(yield)` -like content inclusion/nesting mechanisms. One simple and one complicated exemplar.

#### Considerations, further out:
- Sufficient error reporting.
- Heavy testing of quote nesting and escaping, Clojure reader, Clojure comments.
- Allow for parallel execution, as a non-functional requirement.
- Declare version 1.0.0 once the community deems the ART feature-complete, reliable, and properly documented.
- Provide access to execution context from within the evaluation environment: (render) args. The evaluation stack starting from the page through to the current (yield). `user/*render-context*`
- Infer sensible defaults that can be customized via overrides.
- ClojureScript.
- How to achieve fast runtime performance, fast development & testing feedback loop.
- Sign releases.
- CLI: Option to re-render templates only when newer than their output files.
- CLI: Ability to list rendered file paths without writing à la `--dry-run`
- Explain the value of ART. Compare and contrast with other templating systems. Emphasize symbolic computation, and the importance of providing native idioms at each point along the value chain, for example a web-based production workflow where professionals handle HTML and CSS.
- Delimiter escaping rules.
- Java policies, to make it possible to execute untrusted / unknown code within templates.
- Parsing option mode magic within template content. Example from Jinja: `#jinja2:variable_start_string:'[%', variable_end_string:'%]', trim_blocks: False`
- IDE support for .art files: Eclipse, Emacs, IntelliJ, Vim, VS Code
- Maven plugin for rendering ART templates.
- Cache à la https://github.com/davidsantiago/stencil
- AOT compilation.



## Attributions

- Original implementation by [Vivid Inc.](https://vivid-inc.net)
- [ShimDandy](https://github.com/projectodd/shimdandy), [boot-pods](https://github.com/boot-clj/boot/wiki/Pods), [clj-embed](https://github.com/RutledgePaulV/clj-embed) as reference material regarding the evaluation of Clojure code within a sandboxed runtime.
- Illustration by [Ruxandra](https://www.instagram.com/chocolatechiphelmet/).
- The Boot test code initially mimicked [perun-selmer](https://github.com/rwstauner/perun-selmer).

![](assets/workshop.png)

Of the Clojure templating libraries we identified, none seemed to assist in porting a non-trivial amount of ERB-templated content to a Clojure-based static site generation tool.
We find the ability to in-line arbitrary Clojure code is intoxicatingly pragmatic (also expressed as: Enough rope to hang oneself).
Seeking to wield such expressive power in a general-purpose templating system, we wrote Ash Ra Template, or **ART**.


## License

© Copyright Vivid Inc.
[Apache License 2.0](LICENSE.txt) licensed.
