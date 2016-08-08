(ns service.registry
  (:require [service.common :as common :refer [error?]]
            [service.oauth :as oauth]
            [clojure.core.async :as a :refer [go <! >! timeout chan close!]]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(def registry-url "https://api.stage.yaas.io/haha/registry/v1/aspect-registry")

(defn functions [entity entity-type function-name & options]
  (let [ch (chan)
        url (str registry-url "/entities/" entity "/entityTypes/" entity-type "/functions/" function-name)]
    (http/get url
              {:timeout 2000}
              (common/res-handler ch url))
    ch))
