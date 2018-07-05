(ns hface.charts
  (:require [hface.c3 :refer [gauge map-area q-area]]))

(defn clear-chart [chart]
  (.load chart (clj->js {:unload true})))

(defn cpu-gauge [clazz]
  (gauge clazz {:data-is "cpu usage" :height 80}))

(defn mem-gauge [clazz]
  (gauge clazz {:data-is "memory usage"
                :height 80
                :pattern ["rgba(31, 146, 180, 0.8)"]
                :thresholds []
                :label {:format (fn [v r]
                                  (js/filesize v))
                        :show false}}))

(defn map-area-chart [clazz]
  (map-area clazz))

(defn q-area-chart [clazz]
  (q-area clazz))

(defn thresholds [t-max n]
  (map #(* (float (/ t-max n)) %)
       (range 1 (inc n))))
