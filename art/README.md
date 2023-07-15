# Ash Ra Template ART Library 



[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/net.vivid-inc/art.svg?color=blue&style=flat-square)](https://clojars.org/net.vivid-inc/art)
[![cljdoc](https://cljdoc.org/badge/net.vivid-inc/art)](https://cljdoc.org/d/net.vivid-inc/art)

ART is an expressive & customizable template system for Clojure.
It's **design goals** are:
- Symbolic computation, as contrasted to declarative, non-Turing complete languages. You choose what features you do or don't employ.
- Reasonable minimum [requirements](#requirements).
- Effortlessly composable: Use `(render)` wherever you like.
- No surprises. Reasonable defaults.
- Fast time-to-first-experience; reliable operation over the long term.

_Note_ that until ART achieves version 1.0 status, details may be subject to change.

- [Requirements](#requirements)
- [Quick Start](#quickstart)
- [Template syntax](#template-syntax)
- [Rendering and options](#rendering-and-options)
  - [Providing ``:bindings``](#bindings)
  - [Configurable ``:delimiters``](#delimiters)
  - [Render ``:to-phase``](#to-phase)



<a name="requirements"></a>
## Requirements

ART is tested on:

- Clojure 1.10.0 and newer
- Java LTS releases 8, 11, 17

<a name="quickstart"></a>
## Quick Start

Include this library from Clojars by adding the latest version of ``net.vivid-inc/art`` to your project dependencies:
```clojure
{:deps {net.vivid-inc/art {:mvn/version "0.7.0"}}}         ; Clojure tools deps.edn
:dependencies [[net.vivid-inc/art "0.7.0"]]                ; Leiningen project.clj
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

You might be interested in the ART [Clojure tool](../clj-art/README.md) or [Leiningen plugin](../lein-art/README.md).



<a name="template-syntax"></a>
## Template syntax

### Plain content with no ART-specific syntax
```clojure
(art/render "We are but stowaways aboard a drifting ship, forsaken to the caprices of the wind and currents.")
```
Passed as a string, the rendered output is a byte-perfect mirror of its input:
```
We are but stowaways aboard a drifting ship, forsaken to the caprices of the wind and currents.
```


### Clojure code blocks

You can embed Clojure code within the template by surrounding forms with ``<(`` and ``)>`` tags, on one line:
```clojure
<( (def button-classes [:primary :secondary :disabled]) )>
```
or over many lines:
```clojure
<(
(defn updated-statement
  [date version]
  (format "This document was updated on %s for version %s"
          date version))
)>
```

### Intermixed text and code
``<(=`` and ``)>`` emit the result of evaluation to the rendered template output stream.
An example of intermixing text and Clojure code blocks that demonstrates the expressive power of ART templates:
```clojure
<(
(require '[clojure.string])
(def publication-dates [1987 1989 1992])
(defn cite-dates [xs] (clojure.string/join ", " xs))
)><p>
Chondrichthyes research published in <(= (cite-dates publication-dates) )>.
</p>
```
Renders to:
```html
<p>
Chondrichthyes research published in 1987, 1989, 1992.
</p>
```

### ART templates in detail
**File extension**: All ART components that interpret files, cheerily treat files with the ``.art`` filename extension as ART template files.

**No advanced tag processing**: ART doesn't feature any advanced tag processing, such as conditionals or HTML escaping.
Instead, equivalent processing can be delegated to occur within Clojure code blocks.

**Initial namespace**: The initial namespace within the template evaluation environment is `user`.

**No inference of parentheses**: Within ART tags, parentheses on outer-most forms are not inferred. This keeps the code easier to reason about and aids natural recognition by the human eye, machine processing, and code editing.

**Whitespace**: It's unnecessary to surround delimiter tags with whitespace.
Everything including whitespace in the text portions of the template is preserved.

```
<( Clojure forms -- will be evaluated but not included in rendered output )>

<(= Clojure forms -- replaced with result of evaluation )>
```

``(user/emit xs)``
As in ERB, the ``<(=`` syntax causes the values of the expression arguments to be emitted to the rendered template output.
The same effect can be accomplished with the ``(emit)`` function which is available within templates.
To demonstrate, each of the ART directives in the following template snippet are functionally equivalent in that each emits the string "Splash!" to the rendered output:
```clojure
<( (emit "Splash!") )>

<(= "Splash!" )>

<(= (str "Splash!") )>
```
The `(emit)` variant can mingle with more Clojure forms, while `<(= ... )>` succinctly expresses the intention of emitting a value to the rendered output.
`(emit)` evaluates to `nil`.


<a name="rendering-and-options"></a>
## Rendering and options
ART provides the thread-safe ``(vivid.art/render)`` function which renders an input string containing Ash Ra Template (ART) -formatted content to an output string.
`(render)` takes a template string followed by a variety of optional keyword arguments:

```clojure
(art/render template :bindings     bindings
                     :delimiters   delimiters
                     :to-phase     phase)
```

<a name="bindings"></a>
### Provide ``:bindings``
`(art/render)` accepts an optional map of `:bindings` that are made available to the template for symbol resolution during render.
Bindings are optimal for use with the echo-eval form ``<(=``.

Simple:
```clojure
(def my-bindings {'month "April"
                  'day   5})
(art/render "<(= month )> <(= day )> was a most pleasant, memorable day."
            :bindings my-bindings)
```

More complex:
```clojure
(def labor-tallies [7 24 13 11])
(art/render "<( (def total (apply + periods)) )>
             LEED certification expended a total of <(= total )> human months."
            :bindings {'periods labor-tallies})
```

<a name="delimiters"></a>
### Configure ``:delimiters``
ART's configurable template delimiters use a `lispy` syntax by default.
```clojure
<( (def e 2.7182) )>
The natural number e is approximately <(= e )>
```
A template with the following style of delimiters
```clojure
{| (def e 2.7182) |}
The natural number e is approximately {|= e |}
```
can be specified with `:delimiters` in the optional map argument:
```clojure
(art/render template-str
            :delimiters {:begin-forms "{|"
                         :end-forms   "|}"
                         :begin-eval  "{|="})
```
There is no particular restriction on what can and cannot be used as delimiters, but beware choosing delimiters whose character strings also occur in your document and in Clojure code.
There are several predefined sets in `vivid.art.delimiters` such as `erb`, ``jinja`, `mustache`, and `php` that can be used directly or serve as a starting point for creating your own delimiter sets.

The template syntax parser is lenient in that both `:end-form` or `:end-eval` delimiters will end a Clojure forms block regardless of whether that block began with `:begin-form` or `:begin-eval` delimiters.

<a name="to-phase"></a>
### Render ``:to-phase``
The rendering process of a single template is composed of several phases.
By default rendering completes all phases, but you can have `(render)` stop at an earlier phase.
This is useful for learning ART's internals and for debugging.
The phases are, in order: `:parse`, `:translate`, `:enscript`, `:evaluate`.

```clojure
; Output raw Clojure code that, if evaluated, produces the final rendered output.
(art/render template-str
            :to-phase :enscript)
```



<a name="template-vars"></a>
## Vars and functions available to templates
`vivid.art/*render-context*`: Template evaluation context as a map.

`(vivid.art/render)`: Can be used to embed render portions of a template from other templates. Examples: Page layouts, headers, panels, footers, copyright messages, all common to a set of rendered pages.
`(render)` fully renders the template to a string; this is what is embedded in the caller.
```clojure
<( (emit (art/render (slurp "layouts/main-layout.html.art")
                     :bindings {:header "..."
                                :footer "..."})) )>
```

`(block)`: Internally, this renders all `(emit)`s to a single string, suitable for passing as bindings to an embedded template.

`(yield k)`: Yields a binding under the key `k` from the template bindings as stored in `vivid.art/*render-context*`. If `k` is not found, then the empty string is returned, thus preventing `(emit)` from printing `null`.



<a name="cookbook"></a>
## Cookbook



<a name="nesting"></a>
### Nesting, structuring, composing templates

Primary mechanism:

```clojure
<( (emit (vivid.art/render "path/to/template.art" :bindings {
  :my-block (block )> some block content <( )})) )>
```

`(render)` is a recursive mechanism, providing the functionality of _include_ or _partial_ in other templating systems.
`:bindings` are `(merge)`ed.
No cycle detection is attempted; we rely on the host platform's call stack to overflow if cyclic recursion goes too far.

Finer points:

```clojure
; Embed a file's contents into the current template
<(= slurp "path/to/file.txt" )>

; Embed the rendered content of another template into the current template
<(= (vivid.art/render "path/to/template.art") )>

; These are equivalent:
<(= (get-in vivid.art/*render-context* [:bindings :body] "") )>
<(= (yield :body) )>
<(= (yield :body "") )>

; These are equivalent:
<(= (yield :body )> ... default content ... <( ) )>
<( (if (yields? :body) )><(= (yield :body) )> ... default content ... <( ) )>

; These are equivalent:
<( (emit (vivid.art/render template-content
           :bindings {:abc 123})) )>
<( (emit (vivid.art/render template-content
           :bindings (update-in (get vivid.art/*render-context* :bindings) assoc :abc 123))) )>
```
TODO Automated tests for these examples



### Render a set of pages with a common layout
Layout file `layouts/layout.html.art`:
```clojure
<html>
<head>
    <meta charset="UTF-8">
    <title><(= title )></title>
    <(= (yield :head) )>
</head>
<body>
    <(= (yield :body) )>
    <script src="site.js"/>
</body>
</html>
```

Consuming file `templates/blog.html.art`:
```clojure
<( (emit (vivid.art/render (slurp "layouts/layout.html.art") :bindings {
  :head (block )>
<link href="blog-theme-dark.css" rel="stylesheet">
<link href="blog-theme-light.css" rel="stylesheet">
  <( )
  :body (block )>
<section class="blog-entry">...</section>
  <( )
})) )>
```

__Discussion:__
Named portions are placed in the layout by way of passing rendered `(block)`s as `:bindings`.
While the embedded layout is `(render)`ed, notice that its rendered output is not wrapped with `<(= )>` but instead `<( (emit ... ) )>`. Using the eval-emit form `<(=` would see it closed at the next occurrence of `)>` (start of the first block in this example) which would lead to a malformed template with syntax errors.

__See also:__
[Example](../examples/layout).



## License

© Copyright Vivid Inc.
[Apache License 2.0](LICENSE.txt) licensed.
