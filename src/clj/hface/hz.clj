(ns hface.hz
  (:import [com.hazelcast.core Hazelcast]
           [com.hazelcast.config XmlConfigBuilder]))

(defn new-instance 
  ([] (new-instance nil))
  ([conf]
    (Hazelcast/newHazelcastInstance conf)))

(defn hz-instance 
  ([] 
     (or (first (Hazelcast/getAllHazelcastInstances))
         (new-instance)))
  ([conf]
    (Hazelcast/getOrCreateHazelcastInstance conf)))

;; creates a demo cluster
(defn cluster-of [nodes & {:keys [conf]}]
  (repeatedly nodes #(new-instance conf)))

(defn distributed-objects [hz-instance]
  (.getDistributedObjects hz-instance))

(defn find-all-maps [instance]
  (filter #(instance? com.hazelcast.core.IMap %) 
          (distributed-objects instance)))

(defn hz-map 
  ([name]
    (hz-map name (hz-instance)))
  ([name instance]
    (.getMap instance name)))

;; modified "assoc"
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
