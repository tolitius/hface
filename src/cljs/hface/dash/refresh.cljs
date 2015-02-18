(ns hface.dash.refresh
    (:require [hface.stats :refer [os-mem-used 
                                   map-mem-used 
                                   node-total-memory]]
              [cognitect.transit :as t]
              [hface.tools :refer [info]]
              [goog.net.XhrIo :as xhr]))

(defonce refresh-interval 5000)

(defn refresh-stats [stats]
  (xhr/send "cluster-stats"
            (fn [event]
              (let [t-reader (t/reader :json)
                    res (-> event .-target .getResponseText)]
                (reset! stats (t/read t-reader res))))
            "GET"))

(defn refresh-cpu [stats chart]
  (when chart
    (let [cpu-usage (-> @stats :aggregated 
                               :top 
                               :os-process-cpu-load)]
      (if (and (> cpu-usage 0) 
               (<= cpu-usage 100))
        (.load chart (clj->js {:columns [["cpu usage" cpu-usage]]}))))))

(defn refresh-os-mem [stats chart]
  (when chart
    (let [mem-usage (-> @stats :aggregated 
                               :top
                               os-mem-used)]
      (.load chart (clj->js {:columns [["memory usage" mem-usage]]})))))

(defn refresh-mem [stats chart]
  (when chart
    (let [mem-usage (-> @stats :aggregated
                               :map-stats
                               vals
                               (map-mem-used (node-total-memory stats)))]
      (.load chart (clj->js {:columns [["memory usage" mem-usage]]})))))

(defn update-map-area [m stats chart] 
  (let [{:keys [m-name m-type]} @m]
    (when (and chart (seq m-name) @stats)
      (let [m-name (keyword m-name)
            m-stats (-> @stats :aggregated m-type m-name)]

          (.flow chart (clj->js {:columns [["x" (js/Date.)]
                                           ["puts" (:put-rate m-stats)]
                                           ["hits" (:hit-rate m-stats)]
                                           ["gets" (:get-rate m-stats)]]
                                 :duration 4600}))))))
