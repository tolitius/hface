(ns hface.charts
  (:require [hface.c3.gauge :as gauge]))

(defn map-view []
  {:cpu-gauge (gauge/generate :height 120)})
