(ns service.core
  (:gen-class)
  (:require
    [taoensso.timbre :as log] 
    [environ.core :refer [env]]
    [org.httpkit.server :as srv :refer [send! with-channel]]
    [clojure.core.async :as a :refer [go <! >! timeout]])
  (:use
    [compojure.route :only [files not-found]]
    [compojure.handler :only [site]]
    [compojure.core :only [defroutes GET POST DELETE ANY context]]))

(def default-port 9000)

(defn port [] (try
                (read-string (:port env))
                (catch Exception e 
                  (log/warn "no PORT environment variable set, using default")
                  default-port)))

(defonce server (atom nil))

(defn home [req]
  (with-channel req channel
    (go
      (send! channel 
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body "{}"}))))

(defroutes all-routes
  (GET "/" req (home req))
  (not-found {:status 404 
              :headers {"Content-Type" "text/html"}
              :body    "resource not found"}))

(defn start-server! [port]
  (log/info "server listening on port " port)
  (reset! server (srv/run-server #'all-routes {:port port}))
  true)

(defn stop-server! []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)
    true))

(defn -main []
  (stop-server!)
  (start-server! (port)))
