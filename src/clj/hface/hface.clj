(ns hface
  (:require [hface.hz :refer [all-instances find-all-maps]]
            [hface.stats.hmap :refer [stats]]))

;; just doing maps (IMap) for now
(defn instance-stats [instance]
  (map stats (find-all-maps instance)))

(defn cluster-stats []
  (map instance-stats (all-instances)))
