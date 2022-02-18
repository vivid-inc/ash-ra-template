; Copyright 2020 Vivid Inc.
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;    https://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns vivid.art.cli.delimiters-test
  (:require
    [clojure.test :refer :all]
    [farolero.core :as farolero]
    [vivid.art.cli.args]
    [vivid.art.cli.usage :refer [cli-options]]
    [vivid.art.cli.validate :as validate]
    [vivid.art.delimiters]))

(def ^:const custom-delimiters
  {:begin-forms "{%"
   :end-forms   "%}"
   :begin-eval  "{{"
   :end-eval    "}}"})


;
; CLI args
;

(deftest cli-pre-packaged-var-names
  (doseq [d ['vivid.art.delimiters/erb
             'vivid.art.delimiters/jinja
             'vivid.art.delimiters/mustache
             'vivid.art.delimiters/php]]
    (let [args ["--delimiters" (name d) "test-resources/empty.art"]
          res (vivid.art.cli.args/cli-args->batch args cli-options)]
      (is (= (var-get (resolve d))
             (:delimiters res))))))

(deftest cli-literal-delims-spec
  (are [expected delims]
    (let [args ["--delimiters" delims "test-resources/empty.art"]
          {:keys [delimiters]} (vivid.art.cli.args/cli-args->batch args cli-options)]
      (= expected delimiters))
    ; The string literal and stringified forms of the pre-packaged delimiter sets
    vivid.art.delimiters/erb "{:begin-forms \"<%\" :end-forms \"%>\" :begin-eval \"<%=\"}"
    vivid.art.delimiters/erb (pr-str vivid.art.delimiters/erb)

    vivid.art.delimiters/jinja "{:begin-forms \"{%\" :end-forms \"%}\" :begin-eval \"{{\" :end-eval \"}}\"}"
    vivid.art.delimiters/jinja (pr-str vivid.art.delimiters/jinja)

    vivid.art.delimiters/lispy "{:begin-forms \"<(\" :end-forms \")>\" :begin-eval \"<(=\"}"
    vivid.art.delimiters/lispy (pr-str vivid.art.delimiters/lispy)

    vivid.art.delimiters/mustache "{:begin-eval \"{{\" :end-eval \"}}\"}"
    vivid.art.delimiters/mustache (pr-str vivid.art.delimiters/mustache)

    vivid.art.delimiters/php "{:begin-forms \"<?\" :end-forms \"?>\" :begin-eval \"<?=\"}"
    vivid.art.delimiters/php (pr-str vivid.art.delimiters/php)

    ; and of the custom delimiters used in this test
    custom-delimiters "{:begin-forms \"{%\" :end-forms \"%}\" :begin-eval \"{{\" :end-eval \"}}\"}"
    custom-delimiters (pr-str custom-delimiters)))

(deftest cli-bad-literal-delim-spec
  (are [delims]
    (= 'validate-delimiters
       (let [args ["--delimiters" delims "test-resources/empty.art"]]
         (farolero/handler-case (vivid.art.cli.args/cli-args->batch args cli-options)
                                (:vivid.art.cli/error [_ {:keys [step]}] step))))
    ; Malformed
    ""
    " "
    "nonsense"))


;
; Internal API
;

(deftest unqualified-vars-as-strings
  (are [expected s]
    (= expected
       (validate/validate-delimiters s))
    vivid.art.delimiters/erb      "erb"
    vivid.art.delimiters/jinja    "jinja"
    vivid.art.delimiters/lispy    "lispy"
    vivid.art.delimiters/mustache "mustache"
    vivid.art.delimiters/php      "php"))

(deftest qualified-vars-as-strings
  (are [expected s]
    (= expected
       (validate/validate-delimiters s))
    vivid.art.delimiters/erb      "vivid.art.delimiters/erb"
    vivid.art.delimiters/jinja    "vivid.art.delimiters/jinja"
    vivid.art.delimiters/lispy    "vivid.art.delimiters/lispy"
    vivid.art.delimiters/mustache "vivid.art.delimiters/mustache"
    vivid.art.delimiters/php      "vivid.art.delimiters/php"))

(deftest unqualified-vars-as-symbols
  (are [expected s]
    (= expected
       (validate/validate-delimiters s))
    vivid.art.delimiters/erb      'erb
    vivid.art.delimiters/jinja    'jinja
    vivid.art.delimiters/lispy    'lispy
    vivid.art.delimiters/mustache 'mustache
    vivid.art.delimiters/php      'php
    custom-delimiters             'vivid.art.cli.delimiters-test/custom-delimiters))

(deftest qualified-vars-as-symbols
  (are [expected s]
    (= expected
       (validate/validate-delimiters s))
    vivid.art.delimiters/erb      #'vivid.art.delimiters/erb
    vivid.art.delimiters/jinja    #'vivid.art.delimiters/jinja
    vivid.art.delimiters/lispy    #'vivid.art.delimiters/lispy
    vivid.art.delimiters/mustache #'vivid.art.delimiters/mustache
    vivid.art.delimiters/php      #'vivid.art.delimiters/php
    custom-delimiters             #'custom-delimiters))

(deftest clojure-maps
  (are [expected s]
    (= expected
       (validate/validate-delimiters s))
    vivid.art.delimiters/erb      vivid.art.delimiters/erb
    vivid.art.delimiters/jinja    vivid.art.delimiters/jinja
    vivid.art.delimiters/lispy    vivid.art.delimiters/lispy
    vivid.art.delimiters/mustache vivid.art.delimiters/mustache
    vivid.art.delimiters/php      vivid.art.delimiters/php
    custom-delimiters             custom-delimiters))

(deftest edn-literals
  (are [expected s]
    (= expected
       (validate/validate-delimiters s))
    vivid.art.delimiters/erb      (pr-str vivid.art.delimiters/erb)
    vivid.art.delimiters/jinja    (pr-str vivid.art.delimiters/jinja)
    vivid.art.delimiters/lispy    (pr-str vivid.art.delimiters/lispy)
    vivid.art.delimiters/mustache (pr-str vivid.art.delimiters/mustache)
    vivid.art.delimiters/php      (pr-str vivid.art.delimiters/php)
    custom-delimiters             (pr-str custom-delimiters)))

(deftest bad-values
  (are [s]
    (= 'validate-delimiters
       (farolero/handler-case (validate/validate-delimiters s)
                              (:vivid.art.cli/error [_ {:keys [step]}] step)))
    nil
    ""
    " "
    "nonsense"))
