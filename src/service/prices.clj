(ns service.prices
  (:require [clojure.core.async :as a :refer [go <! >! timeout]]))

(defn prices [product-id price-selector]
  (go
    [{:amount 105.5
      :currency "USD"}]))
