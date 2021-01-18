(ns com.acme.ordering)

(def ^:const magic-number 50)

(defn minimum-order-qty
  "Absent-mindedly calculates the minimum order quantity in a blind
  bid to achieve margin."
  [unit-price unit-weight]
  (Math/ceil (/ magic-number (* unit-price unit-weight))))
