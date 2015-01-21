(ns hface.refresh
  (:require [hface.util :refer [to-transit every]]
            [hface.stats :refer [cluster-stats]]))

(defonce refresh-interval 1) ;; TODO inject from config
(def stats (atom {}))

(defn refresh-stats []
  (every refresh-interval #(reset! stats
                                   (to-transit (cluster-stats)))))

