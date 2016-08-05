(ns service.common
  (:require [clojure.data.json :as json]
            [clojure.core.async :as a :refer [go <! >! timeout chan close!]])
  (:use [slingshot.slingshot :only [try+ throw+]]))

(defn error? [ret]
  (and
    (coll? ret)
    (= (first ret) :error)))

(defn throw-if-error [ret]
  (if (error? ret)
    (let [[_ exception] ret]
      (throw+ exception))
    ret))

(defn res->json [url {:keys [status headers body error]} expected-status]
  (cond
    error [:error {:msg (str "Unable to interact with remote server " url ", an error occured")
                   :cause error}]
    (not= status expected-status) [:error {:msg (str "Remote server " url " responded with unexpected status")
                                           :status {:actual status
                                                    :expected expected-status}
                                           :body body
                                           :cause (str "Expected status " expected-status " but received " status)}]
    :else (try
            (json/read-str body :key-fn keyword)
            (catch Exception e
              [:error {:msg (str "Unable to convert server response from " url " into json")
                       :cause e}]))))

(defn res-handler [ch url & options]
  (let [{:keys [expected-status] :or {expected-status 200}} (apply hash-map options)]
    (fn [res]
      (go
        (>! ch (res->json url res expected-status))
        (close! ch)))))

(defn clj->json
  "Convert data to JSON. If data is
  nil it defaults to {}. The data
  can either be a sequence or a map." 
  ([] "{}") 
  ([data] (if data
            (json/write-str data :escape-slash false)
            (clj->json))))
