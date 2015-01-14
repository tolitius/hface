(ns hface.hz
  (:require [wall.hack :refer [field]]
            [cheshire.core :refer [parse-string]])
  (:import [com.hazelcast.core Hazelcast]
           [com.hazelcast.config XmlConfigBuilder]
           [com.hazelcast.client HazelcastClient]
           [com.hazelcast.client.config ClientConfig]
           [com.hazelcast.management TimedMemberStateFactory]
           [com.hazelcast.instance HazelcastInstanceProxy 
                                   HazelcastInstanceImpl]
           [com.hazelcast.monitor TimedMemberState]))

(defn new-instance 
  ([] (new-instance nil))
  ([conf]
    (Hazelcast/newHazelcastInstance conf)))

(defn all-instances []
  (Hazelcast/getAllHazelcastInstances))

(defn hz-instance 
  ([] 
     (or (first (all-instances))
         (new-instance)))
  ([conf]
    (Hazelcast/getOrCreateHazelcastInstance conf)))

(defn client-instance 
  ([] (client-instance nil))
  ([conf] 
   (HazelcastClient/newHazelcastClient (ClientConfig.))))

;; creates a demo cluster
(defn cluster-of [nodes & {:keys [conf]}]
  (repeatedly nodes #(new-instance conf)))

(defn distributed-objects [hz-instance]
  (.getDistributedObjects hz-instance))

(defn find-all-maps [instance]
  (filter #(instance? com.hazelcast.core.IMap %) 
          (distributed-objects instance)))

;; adds a string kv pair to the local member of this hazelcast instance
(defn add-member-attr [instance k v]
  (-> instance 
    (.getCluster)
    (.getLocalMember)
    (.setStringAttribute k v)))

(defn local-member-by-instance [instance]
  (-> instance 
    (.getCluster)
    (.getLocalMember)))

(defn members-by-instance [instance]
  (-> instance 
    (.getCluster)
    (.getLocalMember)))

(defn hz-map 
  ([name]
    (hz-map name (hz-instance)))
  ([name instance]
    (.getMap instance name)))

(defn instance-stats [instance]
  (let [inst (if (instance? HazelcastInstanceProxy instance)
               (field HazelcastInstanceProxy :original instance)
               instance)]
    (-> inst
      (TimedMemberStateFactory.)
      (.createTimedMemberState)
      (.toJson)
      (str)
      (parse-string))))

;; playground..


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


