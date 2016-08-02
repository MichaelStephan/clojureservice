(ns service.products
  (:require [service.common :as common :refer [error?]]
            [service.oauth :as oauth]
            [clojure.core.async :as a :refer [go <! >! timeout chan close!]]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(def tenant "hugo4")
(def service-url "https://api.yaas.io/hybris/product/v2")
(def products-url (str service-url "/" tenant "/products"))

(defn product [product-id & options]
  (let [{:keys [oauth-token] :as options} (apply hash-map options)
        ch (chan)
        url (str products-url "/" product-id)]
    (http/get url 
              {:timeout 2000
               :oauth-token oauth-token}
              (fn [res]
                (go
                  (>! ch
                      (common/res->json url res 200))
                  (close! ch))))
    ch))

(defn create-product [product & options]
  (let [{:keys [oauth-token] :as options} (apply hash-map options)
        ch (chan)]
    (http/post products-url
              {:timeout 2000
               :headers {"Content-type" "application/json"
                         "Accept" "application/json"}
               :oauth-token oauth-token
               :body (common/clj->json product)} 
               (fn [res]
                (go
                  (>! ch
                      (common/res->json products-url res 201))
                  (close! ch))))
    ch))

(def new-product {:code 456
                  :name {:en "bla bla"}
                  :description {:en 123}
                  :published false
                  :media []
                  :metadata {:version 1
                             :mixins {}}
                  :mixins {}})

(comment
(defn testx1 []
  (go
    (let [{:keys [access_token]}
          (<! (oauth/token-from-client-credentials "no" "no" ["hybris.product_read_unpublished" "hybris.product_create"]))]
      (when access_token
        (println (<! (create-product new-product :oauth-token access_token)))
        #_(println (<! (product "57a0a177007fa1001d5d0d62" :oauth-token access_token))))))))
