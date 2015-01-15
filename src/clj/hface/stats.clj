(ns hface.stats
  (:require [hface.hz :refer [proxy-to-instance]]
            [hface.util :refer [keys-to-keywords do-with-values]]
            [clojure.java.data :as data]
            [cheshire.core :refer [parse-string]]
            [hface.hz :refer [client-instance]])
  (:import  [com.hazelcast.management TimedMemberStateFactory]
            [org.hface InstanceStatsTask]))

(defn instance-stats [instance]
  (-> instance
    proxy-to-instance
    (TimedMemberStateFactory.)
    (.createTimedMemberState)
    (.toJson)
    str
    parse-string
    keys-to-keywords))

(defn raw-cluster-stats [instance]
  (let [member-statuses (-> instance
                          (.getExecutorService "stats-exec-service")
                          (.submitToAllMembers (InstanceStatsTask.)))]
    member-statuses))

(defn cluster-stats 
  ([] 
   (cluster-stats (client-instance)))
  ([instance]
   (map #(.get %) 
        (vals (raw-cluster-stats instance)))))

(defn merge-map-stats [cs]
  (let [ms (map #(-> % :member-state :map-stats) cs)
        ms (->> ms 
                (apply interleave) 
                (group-by first))
        groupped (do-with-values ms #(map second %))]
    (do-with-values groupped #(apply merge-with + %))))

(defn m-stats [m]
  {:map (.getName m) 
   :stats (data/from-java (.getLocalMapStats m))})
