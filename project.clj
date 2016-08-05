(defproject service "0.1.0-SNAPSHOT"
  :description "A simple price"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [http-kit "2.1.18"]
                 [compojure "1.5.1"]
                 [environ "1.0.3"]
                 [com.taoensso/timbre "4.7.3"]
                 [javax.servlet/servlet-api "2.5"]
                 [org.clojure/core.async "0.2.385"]
                 [org.clojure/data.json "0.2.6"]
                 [slingshot "0.12.2"]
                 [com.taoensso/timbre "4.7.0"]]
  :main service.core)
