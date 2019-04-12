# Ash Ra Template

Simplistic template library featuring Clojure language processing with Ruby 2.0 ERB-esque syntax.

**Motivation**: Of the Clojure templating libraries we found, none seemed to directly assist in porting a non-trivial amount of ERB-templated content from [Middleman](https://github.com/middleman/middleman) to a custom Clojure-based static site generation tool.
We find that the ERB syntax contrasts well with Clojure, and being able to in-line arbitrary Clojure code is intoxicatingly pragmatic (also expressed as: Enough rope to hang oneself).
Seeking to wield such expressive power, we wrote Ash Ra Template, or **ART**.

Works with Clojure 1.9 and newer.

## Usage

[![Clojars Project](https://img.shields.io/clojars/v/vivid/ash-ra-template.svg)](https://clojars.org/vivid/ash-ra-template)

Include this library from Clojars by adding ``[vivid/ash-ra-template "0.2.0"]`` to ``:dependencies`` in your ``project.clj``.

Rendering a template string is easy:
```clojure
(require [vivid.art.core :as art])

(art/render "There were <%= (+ 1 2) %> swallows, dancing in the sky.")
```

Or, to render from a file:
```clojure
(art/render (slurp "prelude.html.art"))
```

All Clojure code is evaluated within a sandboxed Clojure runtime courtesy of [ShimDandy](https://github.com/projectodd/shimdandy).

## Examples

### Plain content with no ART-specific syntax ###
```clojure
(art/render "We are but stow-aways aboard a drifting ship, forsaken to the caprices of the wind and currents.")
```
Passed as a string, the rendered output is expected to be a byte-perfect mirror of its input:
```
We are but stow-aways aboard a drifting ship, forsaken to the caprices of the wind and currents.
```


### Clojure code blocks ###

You can embed Clojure code within the template by surrounding forms with ``<%`` and ``%>`` markers, on one line:
```clojure
<% (def button-classes [:primary :secondary :disabled]) %>
```
or over many lines:
```clojure
<%
(require '[hiccup.core])
(defn toc-entry [heading]
  (hiccup.core/html [:li
    [:a#link
      {:href (str "#" (heading :id))} 
      (heading :text)]]))

... more forms ...

%>
```

Here is an example of intermixing text and Clojure code blocks that realizes the full expressive power of ART templates:
```html
<%
(def publication_dates [1987 1989 1992])
%><p>
  Chondrichthyes research published in <%= (clojure.string/join ", " publication_dates) %>.
</p>
```
results in:
```html
<p>
  Chondrichthyes research published in 1987, 1989, 1992.
</p>
```

### Common Constructs


## Reference

Note that until ART achieves version 1.0 status, details may be subject to change.

### Design Goals
**ART is meant to be utterly composable.** Use `render` wherever you like.
**No parens are assumed.** This allows Clojure forms to be kept whole for copy & paste, machine processing, etc.

### API
``(render s)``
Renders an input string containing Ash-Ra Template -formatted content to an output string.

### Templates
It's unnecessary to surround ERB markers with whitespace.
Whitespace in the text portions of the template is preserved.
No parentheses in Clojure code portions are inferred.

```
<% Clojure forms -- will not be included in rendered output %>

<%= Clojure forms -- replaced with result of evaluation %>
```

``(emit x)``
As in ERB, the ``<%=`` syntax causes the value of the expression to be echoed to the rendered template output.
In ART, this echoing is accomplished with the ``emit`` function which is available within the template, and is the same mechanism used by the template library itself.
To demonstrate, the statements in the following template snippet are functionally equivalent in that they both emit the string "Splash!" to the rendered output:

```clojure
<% (emit "Splash!") %>

<%= "Splash!" %>
```




## Goals: The Path to Version 1.0

- Sufficient error reporting, with well-detailed error messages.
- Permit ERB tag syntax literals to occur in templates. Follow ERB's escaping rules: <%% and %%>
- Clarify the mechanics of the template evaluation runtime: dependencies, initial namespace, requires.
- Accept alternative tag nomenclature, defaulting to ERB. Provide examples for Mustache, PHP, and others.
- Accept an optional map of bindings/definitions that are made available for symbol resolution during render.
- Accept dependency overrides, including a specific Clojure version.
- Round out the tests. Particularly, convert some existing templates, and demonstrate iterative table generation.
- Fast runtime performance, fast test feedback.
- Minimal restrictions. Java 1.8 class files (Popular, long-term). Stable Clojure 1.9.0 (Compatibile with clojure.alpha.tools.deps and doesn't cause another App to appear in the macOS doc when run.) 
- Lein and Boot tasks, to assist with adoption.
- Look at https://github.com/adzerk-oss/zerkdown and https://github.com/adzerk-oss/boot-template
- Investigate signing Clojars releases.
- Declare version 1.0.0 once the community deems the codebase feature-complete, reliable, and properly documented.



## Development

ART is structured as a [Leiningen](https://github.com/technomancy/leiningen/) project.

Run the tests with
```bash
lein test
```

or keep a test watch with

```bash
lein test-refresh
```

**Pull requests** in accord with the simplistic goals are welcome.
And include tests, or your contributions almost will certainly become broken later.
Commits must include Signed-off-by indicating acceptance of the [Developer's Certificate of Origin](DCO.txt).
Unproductive behavior such as unkindness towards others is not tolerated.



## Attributions

- [ShimDandy](https://github.com/projectodd/shimdandy), [boot-pods](https://github.com/boot-clj/boot/wiki/Pods), [clj-embed](https://github.com/RutledgePaulV/clj-embed) as reference material regarding the evaluation of Clojure code within a sandboxed runtime.
- Original implementation by [Vivid Inc.](https://vivid-inc.net)



## License

© Copyright Vivid Inc.
[EPL](LICENSE.txt) licensed.
