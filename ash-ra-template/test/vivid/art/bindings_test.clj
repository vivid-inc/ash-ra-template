; Copyright 2019 Vivid Inc.

(ns vivid.art.bindings-test
  (:require
    [clojure.test :refer :all]
    [vivid.art :as art]))

; TODO api: empty bindings. different ways to code bindings map: '{}  {' }

(deftest bindings-styles
  (testing "Quote the entire bindings map"
    (are [expected template opts]
      (= expected (art/render template opts))

      "" "" {:bindings '{}}))
  (testing "Quote individual bindings map keys and/or values"
    (are [expected template opts]
      (= expected (art/render template opts))

      "" "" {:bindings '{}})))

(deftest data-types
  (testing "Unspoiled transport of Clojure data types across the Java classloader and ShimDandy divide"
    (are [expected template opts]
      (= expected (art/render template opts))

      ; Booleans
      "true class java.lang.Boolean false class java.lang.Boolean" "<%= t %> <%= (type t) %> <%= f %> <%= (type f) %>" {:bindings {'t true 'f 'false}}

      ; Characters
      "z" "<%= a %>" {:bindings '{a \z}}

      ; Collections
      ; TODO
      ;"" "<%= () %>" {:bindings '{}}

      ; Integers
      "3" "<%= (+ a b) %>" {:bindings '{a 1 b 2}}

      ; Keywords
      ":im-a-keyword class clojure.lang.Keyword" "<%= (str a \" \" (type a)) %>" {:bindings '{a :im-a-keyword}}

      ; Metadata
      ; TODO
      ;"" "<%= () %>" {:bindings '{}}

      ; nil
      "true" "<%= (= nil a) %>" {:bindings {'a 'nil}}
      "null" "<%= (type a) %>" {:bindings {'a 'nil}}

      ; Strings
      "Mass-produced comedy .." "Mass-produced <%= plain %> .." {:bindings '{plain "comedy"}}
      ".. is \"culture\" dependent" ".. is <%= embedded-quotes %>" {:bindings {'embedded-quotes "\"culture\" dependent"}}

      ; Symbols
      ; TODO lambdas OK, def'ed fn's OK, refs to other things not OK.
      "123" "<%= (my-lambda) %>" {:bindings '{my-lambda #(+ 61 62)}}
      "xyz" "<%= sym %>" {:bindings '{sym 'xyz}}
      "a*b+c!d.e:f-g_h?9" "<%= all-chars %>" {:bindings '{all-chars 'a*b+c!d.e:f-g_h?9}}
      ":vivid.art.bindings-test/namespaced-sym" "<%= with-ns %>" {:bindings {'with-ns ::namespaced-sym}}

      ;"" "<%= () %>" {:bindings '{}}
      ;"" "<%= () %>" {:bindings '{}}
      ;"" "<%= () %>" {:bindings '{}}
      )))
