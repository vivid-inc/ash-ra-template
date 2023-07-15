# art-cli Ash Ra Template CLI Library

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/net.vivid-inc/art-cli.svg?color=blue&style=flat-square)](https://clojars.org/net.vivid-inc/art-cli)
[![cljdoc](https://cljdoc.org/badge/net.vivid-inc/art-cli)](https://cljdoc.org/d/net.vivid-inc/art-cli)

`art-cli` aggregates code common to the translation and processing of [Ash Ra Template](https://github.com/vivid-inc/ash-ra-template) build tool and command line arguments into parameters for ART's Clojure API.

_This API is in flux; please don't expect to be able to rely on a stable API until the 1.0 release._

- [Rendering and options](#rendering-and-options)
    - [External ``:dependencies``](#dependencies)

<a name="rendering-and-options"></a>
## Rendering and options

`art-cli` offers additional rendering options and processing capability in support of CLI tooling.

<a name="dependencies"></a>
### External ``:dependencies``
`art-cli` provides the rendering option `:dependencies`.
In addition to the dependencies configured in the runtime, this option allows you to specify your own set of dependencies for the template evaluation environment.

For example, this is template file that uses Hiccup to output an HTMl document.
It ``require``s namespaces from the `hiccup/hiccup` dependency:
```clojure
<(
(require '[hiccup.core])

(def ^:const toc-headings [{:id 738 :text "Move wing assembly into place"}
                           {:id 740 :text "Connect fuel lines and hydraulics"}
                           {:id 746 :text "Attach wing assembly to fuselage"}])

(defn toc-entry [heading]
  (hiccup.core/html [:li
    [:a#link
      {:href (str "#" (heading :id))}
      (heading :text)]]))
)>
<(= (apply str (map toc-entry toc-headings)) )>
```
The template's external dependencies are specified in Leiningen-style notation with `:dependencies`:
```clojure
(require '[vivid.art.cli])

(def batch {:templates    "src/templates/html"
            :dependencies '[[hiccup/hiccup "1.0.5"]]
            :output-dir   "www"})

(vivid.art.cli/render-batch batch)

;; Or, use a convenience function to render multiple batches:
(vivid.art.cli/render-batches [batch
                               ...])
```
Prior to template rendering, dependencies are resolved and added to the classpath using [pomegranate](https://github.com/clj-commons/pomegranate).
After rendering, the classpath is restored to what it was at the call site.
`art-cli` is configured to resolve dependencies using the `clojars` and `maven-central` Maven repositories.

Caveat emptor:
- Add differing versions of dependencies that are already loaded into the classpath at your own peril.
- The process of restoring the classpath might not be perfect.
