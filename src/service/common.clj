(ns service.common
  (:require [clojure.data.json :as json]))

(defn error? [ret]
  (and
    (coll? ret)
    (= (first ret) :error)))

(defn res->json [url {:keys [status headers body error]} expected-status]
  (cond
    error [:error {:msg (str "Unable to interact with remote server " url ", an error occured")
                   :cause error}]
    (not= status expected-status) [:error {:msg (str "Remote server " url " responded with unexpected status")
                                           :cause (str "Unexpected status " status)}]
    :else (try
            (json/read-str body :key-fn keyword)
            (catch Exception e
              [:error {:msg (str "Unable to convert server response from " url " into json")
                       :cause e}]))))

(defn clj->json
  "Convert data to JSON. If data is
  nil it defaults to {}. The data
  can either be a sequence or a map." 
  ([] "{}") 
  ([data] (if data
            (json/write-str data :escape-slash false)
            (clj->json))))
