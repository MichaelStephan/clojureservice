(ns service.core
  (:gen-class)
  (:require
    [service.common :as common]
    [service.prices :as prices-service]
    [taoensso.timbre :as log] 
    [environ.core :refer [env]]
    [org.httpkit.server :as srv :refer [send! with-channel]]
    [clojure.core.async :as a :refer [go <! >! timeout]]
    [clojure.data.json :as json])
  (:use
    [slingshot.slingshot :only [try+ throw+]]
    [compojure.route :only [files not-found]]
    [compojure.handler :only [site]]
    [compojure.core :only [defroutes GET POST DELETE ANY context]]))

(def default-port 9000)

(defn port [] (try
                (read-string (:port env))
                (catch Exception e 
                  (log/warn "No PORT environment variable set, using default")
                  default-port)))

(defonce server (atom nil))

(defn send-json-res! [channel res & options]
  (let [{:keys [status headers] :or {status 200
                                     headers {"Content-Type" "application/json"}}}
        (apply hash-map options)]
    (when-not (send! channel
                     {:status status 
                      :headers headers 
                      :body (common/clj->json res)})
      (log/warnf "Unable to send http response %s %s" channel res))))

(defn home [req]
  (with-channel req channel
    (go
      (send-json-res! channel {}))))

(defn price [req product-id price-selector]
  (with-channel req channel
    (go
      (send-json-res! channel
                      (<! (prices-service/prices product-id price-selector))))))

(defroutes routes
  (GET "/" req (home req))
  (context "/products/:product-id" [product-id]
           (context "/prices/:price-selector" [price-selector]
             (GET "/" req
                  (price req product-id price-selector))))
  (not-found {:status 404
              :headers {"Content-Type" "application/json"}
              :body (common/clj->json {:message "not found"})}))

(defn start-server! [port]
    (log/info "Server listening on port " port)
    (reset! server (srv/run-server #'routes {:port port}))
  true)

(defn stop-server! []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)
    true))

(defn -main []
  (stop-server!)
  (start-server! (port)))
