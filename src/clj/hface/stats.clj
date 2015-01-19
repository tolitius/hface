(ns hface.stats
  (:require [hface.hz :refer [proxy-to-instance]]
            [hface.util :refer [keys-to-keywords do-with-values]]
            [clojure.java.data :as data]
            [cheshire.core :refer [parse-string]]
            [hface.hz :refer [client-instance]])
  (:import  [com.hazelcast.management TimedMemberStateFactory]
            [com.hazelcast.core HazelcastInstanceAware]
            [java.util.concurrent Callable]
            [java.io Serializable]
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

;; TODO: atom and hz instance are not serializable.. work in progress
;; need this due to a figwheel bug: https://github.com/bhauman/lein-figwheel/issues/68#issuecomment-70163386
(defn instance-stats-task []
  (let [instance (atom nil)]
    (reify 
      HazelcastInstanceAware
        (setHazelcastInstance [_ inst]
          (swap! instance inst))
      Callable
        (call [_]
          (instance-stats @instance))
      Serializable)))

(defn raw-cluster-stats [instance]
  (let [member-statuses (-> instance
                          (.getExecutorService "stats-exec-service")
                          ;; (.submitToAllMembers instance-stats-task))]
                          (.submitToAllMembers (InstanceStatsTask.)))]
    member-statuses))

(defn cluster-stats 
  ([] 
   (cluster-stats (client-instance)))
  ([instance]
   (map #(.get %) 
        (vals (raw-cluster-stats instance)))))

(defn merge-stats [kind c-stats]
  (let [ms (map #(-> % :member-state kind) c-stats)
        ms (->> ms 
                (apply interleave) 
                (group-by first))
        groupped (do-with-values ms #(map second %))]
    (do-with-values groupped #(apply merge-with + %))))

(defn cluster-members [c-stats]
  (-> (filter (comp boolean :master) c-stats) 
    first 
    :member-list))

(defn m-stats [m]
  {:map (.getName m) 
   :stats (data/from-java (.getLocalMapStats m))})
