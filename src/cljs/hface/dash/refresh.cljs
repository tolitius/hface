(ns hface.dash.refresh
    (:require [hface.stats :refer [os-mem-used 
                                   map-mem-used 
                                   node-total-memory]]
              [hface.charts :refer [thresholds]]
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
    (let [{:keys [mem-used mem-total]} (-> @stats :aggregated 
                                                  :top
                                                  os-mem-used)]
      (set! (.-internal.config.gauge_max chart) mem-total)
      ;; (set! (.-internal.config.color_threshold_values chart) (clj->js (thresholds mem-total 4)))
      (.load chart (clj->js {:columns [["memory usage" mem-used]]})))))

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
                                 :duration 2000}))))))

(defn update-q-area [q stats chart] 
  (let [{:keys [q-name q-type]} @q]
    (when (and chart (seq q-name) @stats)
      (let [q-name (keyword q-name)
            q-stats (-> @stats :aggregated q-type q-name)]

          (.flow chart (clj->js {:columns [["x" (js/Date.)]
                                           ["puts" (:put-rate q-stats)]
                                           ["rejected-puts" (:rejected-put-rate q-stats)]
                                           ["takes" (:take-rate q-stats)]
                                           ["empty-takes" (:empty-take-rate q-stats)]]
                                 :duration 4600}))))))
