(ns hface.stats
  (:require [hface.tools :refer [byte-size]]))

;; memory

(defn os-mem-used [{:keys [memory-heap-memory-used
                           memory-non-heap-memory-used
                           os-memory-total-physical-memory]}]
  (-> (+ memory-heap-memory-used memory-non-heap-memory-used)
      (* 100) ;; i.e. 100%
      (/ os-memory-total-physical-memory)))

(defn map-mem-cost [{:keys [owned-entry-memory-cost
                            backup-entry-memory-cost
                            heap-cost]}]
  ;; (.log js/console "owned: " owned-entry-memory-cost ", backups: " backup-entry-memory-cost ", heap: " heap-cost)
  (+ owned-entry-memory-cost backup-entry-memory-cost))

(defn map-mem-used [maps total-memory]
  (-> (reduce + (map map-mem-cost maps))
      (* 100) ;; i.e. 100%
      (/ total-memory)))

(defn node-total-memory [stats]
  (-> @stats :aggregated :top :os-memory-total-physical-memory))


;; members

(defn members [s]
  (map name (-> s :per-node keys)))


;; maps

(defn map-ops [m stats]
  (let [m-stats (-> @stats :aggregated :map-stats m)
        puts (:put-rate m-stats)
        gets (:get-rate m-stats)]
    (+ puts gets)))

(defn map-highlevel [m stats]
  (if (and (seq @m) (seq @stats))
    (let [{:keys [owned-entry-count heap-cost]} 
          (-> @stats :aggregated :map-stats (.get (keyword @m)))]
      {:map-name @m :entries owned-entry-count :mem (byte-size heap-cost)})
      {:map-name "didn't specify" :entries "no entries" :mem "takes no memory"}))
