(ns hface.stats.hmap
  (:require [clojure.string :as s]
            [clojure.java.data :as data]))

(defn stats [m]
  {:map (.getName m) 
   :stats (data/from-java (.getLocalMapStats m))})

#_(defn camel-to-hyphen [k]
  (->> (map s/lower-case 
            (s/split (name k) #"(?=[A-Z])"))
    (s/join "-")
    keyword))
