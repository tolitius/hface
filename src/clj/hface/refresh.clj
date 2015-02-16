(ns hface.refresh
  (:require [hface.util :refer [every]]
            [hface.stats :refer [cluster-stats]]))

(def stats (atom {}))

(defn rate-it 
  ([previous current interval]
    (float (/ (- current previous) interval)))
  ([previous current k interval]
    (float (/ (- (k current) (k previous)) interval))))

;; calculating rates for maps (e.g. hazelcast IMap)

(defn rate-map [pm cm interval]
  (assoc cm :get-rate (rate-it pm cm :get-count interval)
            :put-rate (rate-it pm cm :put-count interval)
            :hit-rate (rate-it pm cm :hits interval)))

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
