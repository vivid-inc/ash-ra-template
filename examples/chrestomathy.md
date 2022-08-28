## (if) condition
When using emit-style stream literals, separate the then- and else- expression clauses with a `<( )>`.
```clojure
<( (if x )>
It is true.
<( )>
May you find truthfulness elsewhere.
<( ) )>
```



## Break the build when assumptions fail
Encode assumptions within a code block.
```clojure
<(
;; Break the build if the following project invariants are in violation.
(assert (< animation-time action-step-time))
)>
```



## Share code between Art templates and ClojureScript
Store the shared code in `.cljc` files.

