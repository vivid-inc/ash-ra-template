# Ash Ra Template

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/vivid/ash-ra-template.svg?color=blue&style=flat-square)](https://clojars.org/vivid/ash-ra-template)
[![CircleCI](https://circleci.com/gh/vivid-inc/ash-ra-template/tree/master.svg?style=svg)](https://circleci.com/gh/vivid-inc/ash-ra-template/tree/master)
[![Codecov](https://codecov.io/gh/vivid-inc/ash-ra-template/branch/master/graph/badge.svg)](https://codecov.io/gh/vivid-inc/ash-ra-template)

Expressive & customizable template system featuring Clojure language processing

Works with Clojure 1.9 and newer.



## Components

[ART library](ash-ra-template/README.md), including detailed information about using the ART library and ART template syntax, rendering API and options, and processing.

[Boot task](boot-art/README.md) for rendering ART templates.

[Leiningen plugin](lein-art/README.md) for rendering ART templates.



## Quickstart

Include this library from Clojars by adding the latest version of ``vivid/ash-ra-template`` to your project dependencies, such as in a Leiningen ``project.clj``:
```clojure
    :dependencies [[vivid/ash-ra-template "0.5.0"]]
```

Render a template string:
```clojure
(require [vivid.art :as art])

(art/render "There were <%= (+ 1 2) %> swallows, dancing in the sky.")
```

Or, to render from a file:
```clojure
(art/render (slurp "prelude.html.art"))
```



## Contributing

**Pull Requests** are welcome!
We work with people offering PRs to revise and iterate leading to solutions in accord with project goals and [release criteria](QUALITY.md).
Commits must include Signed-off-by indicating acceptance of the [Developer's Certificate of Origin](DCO.txt).
Unproductive behavior such as unkindness towards others and derailment is not tolerated.

### The Path to Version 1.0

- ~Default to a subset of ERB syntax (as of Ruby 2.0). Accept alternative tag nomenclature ``:delimiters``. Provide examples for Mustache, PHP, and others.~
- ~Accept an optional map of ``:bindings`` (definitions) that are made available for symbol resolution during render.~
- ~api-contract tests for ``:dependencies``.~
- ~Automated testing on all supported versions of Clojure.~
- ~Boot task for rendering templates.~
- ~Leiningen plugin for rendering templates.~
- ~Document approach to quality assurance.~
- ~Stabilize minimal requirements of the project, including Clojure version and dependencies.~
- ~Expose a public API.~
- ~Documentation organized by project and use.~
- ~Test on the most recent releases of each significant JDK (8 and 11 at the time of this writing).~
- 0.5.0: Provide a direct execution mode in addition to ShimDandy's sandbox.
- 0.5.0: deps.clj run mode.
- 0.5.0: (include) and (yield) -like content inclusion/nesting mechanisms. One simple and one complicated exemplar.
- 0.5.0: Sign releases.
- Explain the value of ART. Compare and contrast with other templating systems. Emphasize symbolic computation, and the importance of providing native idioms at each point along the value chain, for example a web-based production workflow where professionals handle HTML and CSS.
- Delimiter escaping rules.
- Infer sensible defaults that can be customized via overrides.
- Sufficient error reporting + documentation.
- Java policies, to make it possible to execute untrusted / unknown code within templates.
- AOT compilation.
- Declare version 1.0.0 once the community deems the ART feature-complete, reliable, and properly documented.

### Beyond Version 1.0

Consider:
- How to achieve fast runtime performance, fast test feedback.
- Option for parallel execution.
- A parsing option mode magic within template content.
- An option to infer outer-most parens.
- JetBrains IntelliJ IDEA support for .art files.
- Maven plugin for rendering ART templates.



## Attributions

- Original implementation by [Vivid Inc.](https://vivid-inc.net)
- [ShimDandy](https://github.com/projectodd/shimdandy), [boot-pods](https://github.com/boot-clj/boot/wiki/Pods), [clj-embed](https://github.com/RutledgePaulV/clj-embed) as reference material regarding the evaluation of Clojure code within a sandboxed runtime.
- Illustration by [Ruxandra](https://www.instagram.com/chocolatechiphelmet/).
- The Boot test code initially mimicked [perun-selmer](https://github.com/rwstauner/perun-selmer).

![](workshop.png)

Of the Clojure templating libraries we identified, none seemed to assist in porting a non-trivial amount of ERB-templated content to a Clojure-based static site generation tool.
We find the ability to in-line arbitrary Clojure code is intoxicatingly pragmatic (also expressed as: Enough rope to hang oneself).
Seeking to wield such expressive power in a general-purpose templating system, we wrote Ash Ra Template, or **ART**.


## License

© Copyright Vivid Inc.
[Apache License 2.0](LICENSE.txt) licensed.
