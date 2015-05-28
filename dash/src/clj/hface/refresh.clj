(ns hface.refresh
  (:require [clojure.tools.logging :refer [info]]
            [hface.util :refer [every]]
            [hface.stats :refer [cluster-stats]]))

(def stats (atom {}))

(defn rate-it [previous current k rate-key interval]
    (let [rate (float (/ (- (k current) (k previous)) interval))]
      (if-not (neg? rate)        ;; can be negative if one of the nodes parted
        rate
        (rate-key previous))))   ;; in which case return a previous rate

;; calculating rates for maps (e.g. hazelcast IMap/MultiMap)

(defn rate-map [pm cm interval]
  (assoc cm :get-rate (rate-it pm cm :get-count :get-rate interval)
            :put-rate (rate-it pm cm :put-count :put-rate interval)
            :hit-rate (rate-it pm cm :hits :hit-rate interval)))

(defn rate-q [pm cm interval]
  (assoc cm :take-rate (rate-it pm cm :number-of-offers :take-rate interval)
            :put-rate (rate-it pm cm :number-of-polls :put-rate interval)
            :rejected-put-rate (rate-it pm cm :number-of-rejected-offers :rejected-put-rate interval)
            :empty-take-rate (rate-it pm cm :number-of-empty-polls :empty-take-rate interval)))

(defn compare-and-rate [rate previous current interval]
  (into {}
    (for [[k v] current]
      (if-let [pv (k previous)]          ;; if there is a value in a prev map for this key
        [k (rate pv v interval)]
        [k (rate v v interval)]))))

(defn add-ratings [c-stats               ;; current stats
                   p-stats               ;; previous stats
                   rate
                   interval
                   s-type]               ;; :map-stats, :multi-map-stats, :queue-stats
  (let [stats #(-> % :aggregated s-type)
        previous (stats p-stats)
        current (stats c-stats)]
    (assoc-in c-stats [:aggregated s-type]
              (compare-and-rate rate previous current interval))))

;;-----------------------------------------------------------------

(defn refresh-stats 
  "refreshing stats and adding rate metrics"
  [previous interval]
  (let [with-rates (-> (cluster-stats)
                       (add-ratings @previous rate-map interval :map-stats)
                       (add-ratings @previous rate-map interval :multi-map-stats)
                       (add-ratings @previous rate-q interval :queue-stats))]
    (reset! previous with-rates)))

(defn collect-stats 
  "scheduling stats refresh on every 'interval'"
  [interval]
  (every interval #(refresh-stats stats interval)))
