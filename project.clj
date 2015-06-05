(defproject service "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [http-kit "2.1.18"]
                 [compojure "1.3.4"]
                 [environ "1.0.0"]]
  :main service.core
  :profiles { 
             :dev { :dependencies [[javax.servlet/servlet-api "2.5"]]}
             :production { :dependencies [[javax.servlet/servlet-api "2.5"]]}
             })
