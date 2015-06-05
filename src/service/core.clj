(ns service.core
  (:gen-class)
  (:require
    [environ.core :refer [env]]
    [org.httpkit.server :as srv])
  (:use
    [compojure.route :only [files not-found]]
    [compojure.handler :only [site]]
    [compojure.core :only [defroutes GET POST DELETE ANY context]]))

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
  (not-found "<p>Page not found.</p>")) ;; all other, return 404

(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn -main []
  ;; The #' is useful when you want to hot-reload code
  ;; You may want to take a look: https://github.com/clojure/tools.namespace
  ;; and http://http-kit.org/migration.html#reload
  (reset! server (srv/run-server #'all-routes {:port (read-string (env :port))})))
