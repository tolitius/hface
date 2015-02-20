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

(defn compare-and-rate-maps [previous current interval]
  (into {}
    (for [[k v] current]
      (if-let [pv (k previous)]           ;; if there is a value in a prev map for this key
        [k (rate-map pv v interval)]
        [k (rate-map v v interval)]))))

(defn add-map-ratings [p-stats            ;; previous stats
                       c-stats            ;; current stats
                       interval
                       mtype]             ;; :map-stats, :multi-map-stats
  (let [m-stats #(-> % :aggregated mtype)
        previous (m-stats p-stats)
        current (m-stats c-stats)]
    (assoc-in c-stats [:aggregated mtype]
              (compare-and-rate-maps previous current interval))))

;;-----------------------------------------------------------------

(defn refresh-stats 
  "refreshing stats and adding rate metrics"
  [previous interval]
  (let [current (cluster-stats)
        with-map-rates (add-map-ratings @previous current interval :map-stats)
        with-rates (add-map-ratings @previous with-map-rates interval :multi-map-stats)]     ;; TODO: refactor out and add other ratings
    (reset! previous with-rates)))

(defn collect-stats 
  "scheduling stats refresh on every 'interval'"
  [interval]
  (every interval #(refresh-stats stats interval)))
