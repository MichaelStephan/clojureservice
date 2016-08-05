(ns service.products
  (:require [service.common :as common :refer [error?]]
            [service.oauth :as oauth]
            [clojure.core.async :as a :refer [go <! >! timeout chan close!]]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(def product-service-url "https://api.yaas.io/hybris/product/v2") 

(defn products-url [tenant]
  (str product-service-url "/" tenant "/products"))

(defn product-url [tenant product-id]
  (str (products-url tenant) "/" product-id))

(defn product [tenant product-id & options]
  (let [{:keys [oauth-token] :as options} (apply hash-map options)
        ch (chan)
        url (product-url tenant product-id)]
    (http/get url 
              {:timeout 2000
               :oauth-token oauth-token}
              (common/res-handler ch url))
    ch))

(defn create-product [tenant product & options]
  (let [{:keys [oauth-token] :as options} (apply hash-map options)
        ch (chan)
        url (products-url tenant)]
    (http/post url 
              {:timeout 2000
               :headers {"Content-type" "application/json"
                         "Accept" "application/json"}
               :oauth-token oauth-token
               :body (common/clj->json product)} 
               (common/res-handler ch url :expected-status 201))
    ch))

(comment
  (defn testx2 []
    (go
      (let [{:keys [access_token]}
            (<! (oauth/token-from-client-credentials "id" "secret" ["hybris.product_read_unpublished"]))]
        (when access_token
          (println (<! (product "hugo4" "57a1d326289c48001daef0c4" :oauth-token access_token)))))))

  (def new-product {:code 4760
                    :name {:en "bla bla"}
                    :description {:en 123}
                    :metadata {:mixins {:entitytype "https://api.yaas.io/hybris/schema/v1/hugo4/entitytype"}}
                    :mixins {:entitytype {:name "subscriptionproduct"}}})

  (defn testx1 []
    (go
      (let [{:keys [access_token] :as resp}
            (<! (oauth/token-from-client-credentials "id" "secret" ["hybris.product_read_unpublished" "hybris.product_create"]))]
        (if access_token
          (println (<! (create-product "hugo4" new-product :oauth-token access_token)))
          (println resp))))))
