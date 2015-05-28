(ns hface.util
  (:require [cognitect.transit :as transit]
            [clojure.tools.logging :refer [error]]
            [clojure.string :as s])
  (:import [java.io ByteArrayOutputStream]
           [java.util.concurrent Executors TimeUnit]))

;; schedule a function to run every.. TimeUnit
(defn every
  ([interval fun] 
   (every interval fun TimeUnit/SECONDS))
  ([interval fun time-unit] 
   (let [f #(try (fun) (catch Exception e (error (.printStackTrace e System/out))))]
    (.scheduleAtFixedRate (Executors/newScheduledThreadPool 1) 
      f 0 interval time-unit))))

;; a shorthand to write out data to transit
;; adopted from "https://github.com/jalehman/ring-transit"
(defn to-transit 
  ([data] 
   (to-transit data :json {}))
  ([x t opts]
    (let [baos (ByteArrayOutputStream.)
          w    (transit/writer baos t opts)
          _    (transit/write w x)
          ret  (.toString baos)]
      (.reset baos)
      ret)))

;; given a CamelCased string, converts it to hyphenated keyword
(defn camel-to-hyphen [k]
  (->> (map s/lower-case 
            (s/split (name k) #"(?=[A-Z])"))
    (s/join "-")
    keyword))

(defn keys-to-keywords [m]
  (into {} 
        (for [[k v] m] 
          [(camel-to-hyphen k) (if (map? v)
                                 (keys-to-keywords v)
                                 v)])))

(defn do-with-values [m f]
  (into {} 
        (for [[k v] m] 
          [k (f v)])))

;; modified "assoc" for IMap/Map
(defn put!
  ([m k v] (doto m (.put k v)))
  ([m k v & kvs]
    (let [ret (doto m (.put k v))]
      (if kvs
        (if (next kvs)
          (recur ret (first kvs) (second kvs) (nnext kvs))
          (throw (IllegalArgumentException.
                  "put expects even number of arguments after map/vector, found odd number")))
        ret))))
