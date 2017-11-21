# Ash Ra Template

Minimal template library for Clojure featuring Ruby 2.0 ERB syntax and Clojure language processing.

**Motivation**: Of the Clojure templating libraries we found, none seemed to directly assist in porting a non-trivial amount of ERB-templated content from [Middleman](https://github.com/middleman/middleman) to a custom Clojure-based static site generation tool.
We find that the ERB syntax contrasts well with Clojure code, and being able to in-line arbitrary Clojure code is intoxicatingly pragmatic (also expressed as: Enough rope to hang oneself).
So to keep this expressive power we wrote Ash Ra Template, or **ART**.


## Usage

[![Clojars Project](https://img.shields.io/clojars/v/vivid/ash-ra-template.svg)](https://clojars.org/vivid/ash-ra-template)

Note that until ART achieves version 1.0 status, the API may be subject to change.

Include this library from Clojars by adding ``[vivid/ash-ra-template "0.1.0"]`` to ``:dependencies`` in your ``project.clj``.

Rendering a template string is easy:
```clojure
(require [vivid.art.core :as art])

(art/render "There were <%= (+ 1 2) %> swallows, dancing in the sky.")
```

Or, to render from a file:
```clojure
(art/render (slurp "resources/epilogue.html.art"))
```

Examples
--------

### Plain template with no ERB-specific syntax ###
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
(defn join [sep xs]
      (apply str (interpose sep xs)))
%>
<p>
  Chondrichthyes research published in <%= (join ", " publication_dates) %>.
</p>
```
results in:
```html
<p>
  Chondrichthyes research published in 1987, 1989, 1992.
</p>
```

Note that it's unnecessary to surround ERB markers with whitespace, that whitespace in the text portions of the template is preserved, and that no parentheses in Clojure code portions are inferred.


### The emit function ###
As in ERB, the ``<%=`` syntax causes the value of the expression to be echoed to the rendered template output.
In ART, this echoing is accomplished with the ``emit`` function which is available within the template, and is the same mechanism used by the template library itself.
To demonstrate, the statements in the following template snippet are functionally equivalent in that they both emit the string "Splash!" to the rendered output:

```clojure
<% (emit "Splash!") %>

<%= "Splash!" %>
```


## Goals: The Path to Version 1.0

- Implement full ERB syntax as of Ruby 2.0.
- Excellent error reporting.
- Mechanism for ERB syntax to occur in templates without triggering the parser, perhaps by escaping. Follow ERB's lead.
- Accept an optional hash of definitions that are made available for symbol resolution during render.
- Round out the tests. Particularly, convert some existing templates, and demonstrate iterative table generation.
- Declare version 1.0.0 once the community deems the codebase feature-complete, reliable, and properly documented.

Nice to have:
- ClojureScript support.
- A fully streaming, lazy implementation.


## Development

Run the tests with

```
lein test
```

or keep a test watch with

```
lein test-refresh
```

**Pull requests** in accord with the minimalist goals are welcome.
And include tests, or your contributions almost will certainly become broken later.
Commits must include Signed-off-by indicating acceptance of the [Developer's Certificate of Origin](DCO.txt).


## License

© Copyright Vivid Inc.
[EPL](LICENSE.txt) licensed.
