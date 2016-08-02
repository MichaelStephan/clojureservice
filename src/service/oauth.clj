(ns service.oauth
  (:require [service.common :as common]
            [clojure.core.async :as a :refer [go <! >! timeout chan close!]]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(def token-url "https://api.yaas.io/hybris/oauth2/v1/token") 

(defn token-from-client-credentials [client-id client-secret scopes]
  (let [ch (chan)]
    (http/post token-url
              {:timeout 2000
               :form-params {:grant_type "client_credentials"
                             :client_id client-id
                             :client_secret client-secret 
                             :scope (clojure.string/join " " scopes)}}
              (fn [res]
                (go
                  (>! ch
                      (common/res->json token-url res 200))
                  (close! ch))))
   ch))
