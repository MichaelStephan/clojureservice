(ns service.prices
  (:require 
    [service.common :as common]
    [service.products :as products]
    [service.oauth :as oauth]
    [taoensso.timbre :as log]
    [service.registry :as registry]
    [clojure.core.async :as a :refer [go <! >! timeout]])
  (:use [slingshot.slingshot :only [try+ throw+]]))

(def entity "yaas-product")
(def default-entity-type "yaas-product")

(defn prices-token [tenant]
  (oauth/token-from-client-credentials "id" "secret" ["hybris.product_read_unpublished"]))

(defn prices [tenant product-id price-selector]
  (go
    (try+ 
      (let [{:keys [access_token]} (-> (prices-token tenant)
                                       (<!)
                                       (common/throw-if-error))
            entity-type (-> (products/product tenant product-id :oauth-token access_token)
                            (<!)
                            (common/throw-if-error)
                            (get-in [:mixins :entitytype :name] default-entity-type))
            price-fns (-> (registry/functions "yaas-product" entity-type "getPrice")
                           <!)]
        (when (not= (count price-fns) 1)
          (throw+ {:cause "There is no or there are more than one price functions configured"
                   :details price-fns}))
        (let [{:keys [security url]} (first price-fns)
              {:keys [yaas]} security
              {:keys [clientId clientSecret scopes]} yaas]
            (println clientId clientSecret scopes url)))
      (catch Object e
        [:error {:cause e}]))))

(comment
  (defn testx1 []
    (go
      (println )))
  (go
    [{:amount 105.5
      :currency "USD"}]))
