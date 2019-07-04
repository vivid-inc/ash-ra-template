# Ash Ra Template ART Library

[![Clojars Project](https://img.shields.io/clojars/v/vivid/ash-ra-template.svg)](https://clojars.org/vivid/ash-ra-template)

ART is an expressive template system for Clojure.
It's **design goals** are:
- Symbolic computation, as contrasted to declarative, non-Turing complete languages. You choose what features you do or don't employ.
- Reasonable minimum [requirements](#requirements)
- Effortlessly composable: Use `(render)` wherever you like.
- No surprises. Reasonable defaults.
- Fast time-to-first-experience; reliable operation over the long term.

_Note_ that until ART achieves version 1.0 status, details may be subject to change.

- [Requirements](#requirements)
- [Quickstart](#quickstart)
- [Template syntax](#template-syntax)
- [Rendering and options](#rendering-and-options)
  - [Providing ``:bindings``](#bindings)
  - [Configurable ``:delimiters``](#delimiters)
  - [External ``:dependencies``](#dependencies)



<a name="requirements"></a>
## Requirements

- Java 8 and all subsequent LTS releases (currently: Java 8 and Java 11). Java 8, because it strikes a good balance between wide adoption and long-term stability.
- Clojure 1.9.0, for [spec](https://clojure.org/guides/spec), and because it is compatible with a ``clojure.alpha.tools.deps`` version that has reasonable Maven-style dependency resolving abilility.

<a name="quickstart"></a>
## Quickstart

Include this library from Clojars by adding ``[vivid/ash-ra-template "0.4.0"]`` to your project dependencies, such as in a Leiningen ``project.clj``:
```clojure
  :dependencies [[vivid/ash-ra-template "0.4.0"]]
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

You might be interested in the ART [Boot task](../boot-art/README.md) or [Leiningen plugin](../lein-art/README.md).



<a name="template-syntax"></a>
## Template syntax

### Plain content with no ART-specific syntax
```clojure
(art/render "We are but stowaways aboard a drifting ship, forsaken to the caprices of the wind and currents.")
```
Passed as a string, the rendered output is a byte-perfect mirror of its input:
```
We are but stow-aways aboard a drifting ship, forsaken to the caprices of the wind and currents.
```


### Clojure code blocks

You can embed Clojure code within the template by surrounding forms with ``<%`` and ``%>`` tags, on one line:
```clojure
<% (def button-classes [:primary :secondary :disabled]) %>
```
or over many lines:
```clojure
<%
(defn updated-statement
  [date version]
  (format "This document was updated on %s for version %s"
          date version))
%>
```

### Intermixed text and code
``<%=`` and ``%>`` emit the result of evaluation to the rendered template output stream.
An example of intermixing text and Clojure code blocks that demonstrates the expressive power of ART templates:
```clojure
<%
(require '[clojure.string])
(def publication-dates [1987 1989 1992])
(defn cite-dates [xs] (clojure.string/join ", " xs))
%><p>
Chondrichthyes research published in <%= (cite-dates publication-dates) %>.
</p>
```
Renders to:
```html
<p>
Chondrichthyes research published in 1987, 1989, 1992.
</p>
```

### ART templates in detail
**File extension**: All ART components interpret files with the ``.art`` filename extension as ART template files.

**Sandboxed execution**: All Clojure code embedded in templates is evaluated within a sandboxed Clojure runtime courtesy of [ShimDandy](https://github.com/projectodd/shimdandy).

**No advanced tag processing**: ART doesn't feature any advanced tag processing, such as conditionals or HTML escaping.
Instead, equivalent processing can be delegated to occur within Clojure code blocks.

**Initial namespace**: The initial namespace within the template evaluation environment is `user`.

**Whitespace**: It's unnecessary to surround delimiter tags with whitespace.
Everything including whitespace in the text portions of the template is preserved.

```
<% Clojure forms -- will be evaluated but not included in rendered output %>

<%= Clojure forms -- replaced with result of evaluation %>
```

**No inference of parentheses**: Within ART tags, parentheses on outer-most forms are not inferred. This keeps the code easier to reason about and aids natural recognition by the eye, machine processing, and code editing.

``(user/emit x)``
As in ERB, the ``<%=`` syntax causes the value of the expression to be emitted to the rendered template output.
The same effect can be accomplished with the ``(emit)`` function which is available within templates.
To demonstrate, each of the ART directives in the following template snippet are functionally equivalent in that each emits the string "Splash!" to the rendered output:
```clojure
<% (emit "Splash!") %>

<%= "Splash!" %>

<%= (str "Splash!") %>
```
The `(emit)` variant can mingle with more Clojure forms, while `<%= ... %>` succinctly expresses the intention of emitting a value to the rendered output.



<a name="rendering-and-options"></a>
## Rendering and options
ART provides the ``(vivid.art/render)`` function which renders an input string containing Ash Ra Template (ART) -formatted content to an output string.
`(render)` takes several options:

```clojure
(art/render template {:bindings bindings
                      :delimiters delimiters
                      :dependencies deps})
```

<a name="bindings"></a>
### Providing ``:bindings``
`(art/render)` accepts an optional map of `:bindings` that are made available to the template for symbol resolution during render.
Bindings are optimal for use with echo-eval.

Simple:
```clojure
(def my-bindings {'month "April"
                  'day   5})
(art/render "<%= month %> <%= day %> was a most pleasant, memorable day."
            {:bindings my-bindings})
```

More complex:
```clojure
(def certification-periods [7 24 13 11])
(art/render "<% (def total (apply + periods)) %>
             LEED certification expended a total of <%= total %> human months."
            {:bindings {'periods certification-periods}})
```

<a name="delimiters"></a>
### Configurable ``:delimiters``
ART's configurable template delimiters default to ERB-esque syntax.
```clojure
<% (def e 2.7182) %>
The natural number e is approximately <%= e %>
```
A template with the following style of delimiters
```clojure
{| (def e 2.7182) |}
The natural number e is approximately {|= e |}
```
can be rendered by specified with `:delimiters` in the optional map argument:
```clojure
(art/render template-str
            {:delimiters {:begin-forms "{|"
                          :end-forms   "|}"
                          :begin-eval  "{|="}})
```
There is no particular restriction on what can and cannot be used as delimiters, but beware choosing delimiters whose character strings also occur in your document and in Clojure code.
There are several predefined sets in `vivid.art.delimiters` such as `jinja`, `mustache`, and `php` that can be used directly or serve as a starting point for creating your own delimiter sets.

The template syntax parser is lenient in that both `:end-form` or `:end-eval` delimiters will end a Clojure forms block regardless of whether that block began with `:begin-form` or `:begin-eval` delimiters.

<a name="dependencies"></a>
### External ``:dependencies``
Given a template that ``require``s namespaces from external dependencies in Clojure, such as:
```clojure
<%
(require '[hiccup.core])

(def ^:const toc-headings [{:id 739 :text "Moving wing assembly in place"}
                           {:id 740 :text "Connecting fuel lines and hydraulics"}
                           {:id 741 :text "Attaching wing assembly to fuselage"}])

(defn toc-entry [heading]
  (hiccup.core/html [:li
    [:a#link
      {:href (str "#" (heading :id))}
      (heading :text)]]))
%>
<%= (apply str (map toc-entry toc-headings)) %>
```
The template's external dependencies can be specified as a Clojure deps [lib map](https://clojure.org/reference/deps_and_cli) with `:dependencies` in the option map argument:
```clojure
(art/render template
            {:dependencies {'hiccup {:mvn/version "1.0.5"}}})
```
Dependencies are resolved prior to template rendering using Clojure's ``org.clojure/tools.deps.alpha``.

As an implicit dependency, the template execution environment provides ART's minimum supported version of Clojure, version 1.9.0, but this can be overridden using the same mechanism:
```clojure
            {:dependencies {'org.clojure/clojure {:mvn/version "1.10.0"}}}
```
