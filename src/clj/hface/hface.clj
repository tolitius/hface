(ns hface
  (:require [hface.hz :refer [all-instances find-all-maps]]
            [hface.stats.hmap :refer [stats]]))

;; just doing maps (IMap) for now
(defn instance-stats [instance]
  (map stats (find-all-maps instance)))

(defn cluster-stats []
  ;; submit callables to all the members
  ;; each callable returns instance (e.g. a member) stats
  ;; group stats by instances
  ;; group stats across instances (e.g. cluster wide stats)
  (map instance-stats (all-instances)))


