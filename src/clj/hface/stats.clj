(ns hface.stats
  (:require [hface.hz :refer [proxy-to-instance client-instance]]
            [hface.util :refer [keys-to-keywords do-with-values]]
            [clojure.java.data :as data]
            [clojure.tools.logging :refer [warn]]
            [cheshire.core :refer [parse-string]])
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

(defn- member-statuses [instance]
  (-> instance
      (.getExecutorService "stats-exec-service")
      ;; (.submitToAllMembers instance-stats-task))]
      (.submitToAllMembers (InstanceStatsTask.))))

(defn per-instance-stats [instance]
  (into {} 
    (map (fn [futr]
           (let [stats (.get futr)]   ;; getting stats for an instance future
             {(-> stats 
                  :member-state 
                  :address 
                  keyword) 
              stats}))                ;; {:192.168.1.9:5703 {...}, ...}
      (vals (member-statuses instance)))))

(defn merge-stats [kind i-stats]
  (let [ms (map #(-> % :member-state kind) i-stats)
        ms (->> ms 
                (apply interleave) 
                (group-by first))
        groupped (do-with-values ms #(map second %))]
    {kind (do-with-values groupped #(apply merge-with + %))}))

(defn aggregate-across-cluster [i-stats]
  (into {} 
        (map #(merge-stats % i-stats) 
          [:map-stats 
           :multi-map-stats 
           :queue-stats 
           :topic-stats 
           :executor-stats])))

(defn cluster-stats []
   (let [i-stats (per-instance-stats (client-instance))]
     {:per-node i-stats
      :aggregated (aggregate-across-cluster (vals i-stats))}))

(defn m-stats [m]
  {:map (.getName m) 
   :stats (data/from-java (.getLocalMapStats m))})
