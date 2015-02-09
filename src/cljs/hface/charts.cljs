(ns hface.charts
  (:require [hface.c3 :refer [gauge map-area]]))

(defn map-view []
  {:cpu-gauge (gauge :cpu-usage {:data-is "cpu usage" :height 120})
   :map-stats (map-area :map-stats)})
