# Ash Ra Template Changelog

## 0.4.0
- Changed template parsing mechanism from regular expressions to instaparse.
- Template delimiter definitions can be changed from the default ERB-style with `(render :delimiters {...})`.

## 0.3.0
- API breaking change: Simplified the renderer namespace from `vivid.art.core/render` to `vivid.art/render`.
- Split the project into two sub-projects: `ash-ra-template` the library, and a Boot plugin `boot-art`.
- Defining the initial evaluation environment of ART template code as `user`.

## 0.2.0
- Addition of automated tests.
- Switch from `eval-soup` to an embedded evaluation environment based on ShimDandy.

## 0.1.0
- Use `eval-soup` to render templates featuring Clojure language processing with ERB 2.0-esque syntax.
