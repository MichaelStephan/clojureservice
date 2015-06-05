(ns service.core
  (:gen-class)
  (:require
    [taoensso.timbre :as log] 
    [environ.core :refer [env]]
    [org.httpkit.server :as srv])
  (:use
    [compojure.route :only [files not-found]]
    [compojure.handler :only [site]]
    [compojure.core :only [defroutes GET POST DELETE ANY context]]))

(def default-port 9000)

(def port (try
            (read-string (:port env))
            (catch Exception e 
              (log/warn "no PORT environment variable set, using default")
              default-port)))

(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "hello HTTP!"})

(defn home []
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body "{}"})

(defroutes all-routes
  (GET "/" [] (home))
  (not-found {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "resource not found"}))

(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn -main []
  (log/info "server listening on port " port)
  (reset! server (srv/run-server #'all-routes {:port port})))
