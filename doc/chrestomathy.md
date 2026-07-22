# Chrestomathy

## Conditions

When using emit-style stream literals, separate the then- and else- expression clauses with a `<( )>`.

```clojure
<( (if x )>
It is true.
<( )>
May you find truthfulness elsewhere.
<( ) )>
```

or with `cond`:

```clojure
<( (cond
     (= item :door-key--sparkling) )>Door key beset with sparkling jewels<(
     (and (= item :door-key--skeleton)
          (> remaining-usages 3)) )>Fragile-looking skeleton key<(
     (= item :door-key--skeleton) )>Skeleton key<( ) )>
```

## Break the build when assumptions fail

Encode assumptions within a code block. When violated, the build (or at least template rendering) will fail.

```clojure
<(
;; Break the build if the following project invariants are in violation.
(assert (< animation-time action-step-time))
)>
```

## Share code between Art templates and ClojureScript

Store the shared code in `.cljc` files.
