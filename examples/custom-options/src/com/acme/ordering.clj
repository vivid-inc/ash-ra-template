(ns com.acme.ordering)

(def ^:const minimum-purchase-dollars 50)

(defn minimum-order-qty
  "Absent-minded calculation of the minimum purchase price."
  [unit-price]
  (int (Math/ceil (/ (float minimum-purchase-dollars) (float unit-price)))))
