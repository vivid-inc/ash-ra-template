; Copyright 2023 Vivid Inc. and/or its affiliates.
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     https://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns ^:internal-api vivid.art.delimiters
  "Definitions of template delimiter tags in the style of Ruby ERB
  and various other templating systems.")

(def ^:const erb
  "Syntactically resemblant systems:
  - Active Server Pages or ASP, https://en.wikipedia.org/wiki/Active_Server_Pages
  - Embedded Ruby or ERB, https://github.com/ruby/ruby/blob/trunk/lib/erb.rb
  - JavaServer Pages or JSP, https://en.wikipedia.org/wiki/JavaServer_Pages"
  {:begin-forms "<%"
   :end-forms   "%>"
   :begin-eval  "<%="})

(def ^:const jinja
  "Syntactically resemblant systems:
  - Jinja2, http://jinja.pocoo.org/docs/2.10/templates/
  - Django, https://docs.djangoproject.com/en/2.2/ref/templates/language/"
  {:begin-forms "{%"
   :end-forms   "%}"
   :begin-eval  "{{"
   :end-eval    "}}"})

(def ^:const lispy
  "Default delimiters in ART"
  {:begin-forms "<("
   :end-forms   ")>"
   :begin-eval  "<(="})

(def ^:const mustache
  "Referencing https://mustache.github.io/mustache.5.html"
  {:begin-eval "{{"
   :end-eval   "}}"})

(def ^:const php
  "Referencing https://www.php.net/manual/en/language.basic-syntax.phptags.php"
  {:begin-forms "<?"
   :end-forms   "?>"
   :begin-eval  "<?="})
