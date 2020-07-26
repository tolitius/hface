(ns hface.stats
  (:require [chazel.core :refer [unproxy client-instance]]
            [hface.util :refer [keys-to-keywords do-with-values]]
            [cprop.core :refer [load-config]]
            [clojure.java.data :as data]
            [clojure.tools.logging :refer [warn]]
            [cheshire.core :refer [parse-string]]
            [clojure.string :as cstr])
  (:import  ;; [com.hazelcast.internal.management TimedMemberStateFactory]
            [com.hazelcast.core HazelcastInstanceAware]
            [java.util.concurrent Callable]
            [java.io Serializable]
            [org.hface InstanceStatsTask]))

(defn- member-statuses [instance]
  (try
    (-> instance
        (.getExecutorService "stats-exec-service")
        ;; (.submitToAllMembers instance-stats-task))]
        (.submitToAllMembers (InstanceStatsTask.)))
    (catch Throwable t
      (warn "could not submit instance task via instance [" instance "]: " (.getMessage t)))))


(defn per-instance-stats [instance]
  (into {}
    (map (fn [[host futr]]
           (try
             (let [stats (-> (.get futr)
                           parse-string
                           keys-to-keywords)]         ;; getting stats for an instance future
               {(-> stats
                    :member-state
                    :address
                    keyword)
                stats})                               ;; {:192.168.1.9:5703 {...}, ...}
           (catch Throwable t
             (warn "could not read stats from node [" host "]: " (.getMessage t)))))
      (member-statuses instance))))

(defn merge-nested
  "to aggregate nested maps: i.e. [{:map-stats {:near-cache ...}}
                                   {:map-stats {:near-cache ...}}]"
  [v1 v2]
  (let [add (fn [x y] (if (number? x)
                        (+' x y)
                        x))]
    (if-not (map? v1)
      (add v1 v2)
      (apply merge-with add [v1 v2]))))

(defn merge-stats [kind i-stats]
  (let [ms (map #(-> % :member-state kind) i-stats)
        ms (->> ms
                (apply interleave)
                (group-by first))
        groupped (do-with-values ms #(map second %))]
    {kind (do-with-values groupped #(apply merge-with merge-nested %))}))

(defn aggregate-across-cluster [i-stats]
  (into {}
        (map #(merge-stats % i-stats)
          [:map-stats
           :multi-map-stats
           :queue-stats
           :topic-stats
           :executor-stats])))

(defn non-negative-average [xs]
  (let [nums (filter (comp not neg?) xs)]
    (if (seq nums)
      (float (/ (reduce + nums) (count nums)))
      0)))

(defn no-dots [k-word]
  (-> k-word
      name
      (cstr/replace #"\." "-")
      keyword))

(defn aggregate-top [stats]
  (into {}
    (if (> (count stats) 1)    ;; more than one node in the cluster

      ;; multiple nodes
      (for [[k v] (apply merge-with +                             ;; replace "+" with "vector" and enable average top below if needed
                         (map (comp :runtime-props :member-state)
                              (vals stats)))]
        ;; [(no-dots k) (non-negative-average (flatten v))])      ;; average top per node
        [(no-dots k) v])                                          ;; overall aggregated cluster top

      ;; single node
      (for [[k v] (-> stats vals first :member-state :runtime-props)]
        [(no-dots k) v]))))

(defn with-top [instance-stats aggr-stats]
  (let [top-stats (aggregate-top instance-stats)]
    (assoc aggr-stats :top top-stats)))

(def hz-instance
  (delay
    (let [conf (load-config)]
      (client-instance (conf :hz-client)))))

(defn cluster-stats []
   (let [i-stats (per-instance-stats @hz-instance)
         a-stats (aggregate-across-cluster (vals i-stats))]
     {:per-node i-stats
      :aggregated (with-top i-stats a-stats)}))

(defn m-stats [m]
  {:map (.getName m)
   :stats (data/from-java (.getLocalMapStats m))})




;; playground (currently is solved by hface-client.jar: i.e. no Clojure deps on HZ cluster)

#_(defn instance-stats [instance]
  (-> instance
    unproxy
    (TimedMemberStateFactory.)
    (.createTimedMemberState)
    (.toJson)
    str))

;; TODO: atom and hz instance are not serializable.. work in progress
;; need this due to a figwheel bug: https://github.com/bhauman/lein-figwheel/issues/68#issuecomment-70163386
#_(defn instance-stats-task []
  (let [instance (atom nil)]
    (reify
      HazelcastInstanceAware
        (setHazelcastInstance [_ inst]
          (swap! instance inst))
      Callable
        (call [_]
          (instance-stats @instance))
      Serializable)))

