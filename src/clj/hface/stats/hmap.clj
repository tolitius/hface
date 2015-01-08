(ns hface.stats.hmap
  (:import [com.hazelcast.monitor LocalMapStats]))

(defn stats [m]
  (.getLocalMapStats m))

(defn put-count [^LocalMapStats stats]
  (.getPutOperationCount stats))

(defn get-count [^LocalMapStats stats]
  (.getGetOperationCount stats))
