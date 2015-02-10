(ns hface.stats
    (:require [reagent.core :as reagent :refer [atom]]
              [hface.charts :as charts-for]
              [cognitect.transit :as t]
              [goog.net.XhrIo :as xhr]))

(defonce refresh-interval 2000)

(defn refresh-stats [stats]
  (xhr/send "cluster-stats"
            (fn [event]
              (let [t-reader (t/reader :json)
                    res (-> event .-target .getResponseText)]
                (reset! stats (t/read t-reader res))))
            "GET"))

(defn refresh-top [stats {:keys [cpu-gauge] :as chart}]
  (when chart
    (let [cpu-usage (-> @stats :aggregated 
                               :top 
                               :os.process-cpu-load)]
      (.load cpu-gauge (clj->js {:columns [["cpu usage" cpu-usage]]})))))

(def s (atom -1))

(defn update-map-area [stats m-name {:keys [map-stats] :as chart}]
  (when chart
    (let [m-stats (-> @stats :aggregated 
                             :map-stats
                             :appl)]
                             ;; (.get (keyword m-name)))]
      
      (.flow map-stats (clj->js {:columns [;;["x" (.getSeconds (js/Date.))]
                                           ["x" (swap! s #(+ % 2))]
                                           ["puts" (:put-rate m-stats)]
                                           ["hits" (:hit-rate m-stats)]
                                           ["gets" (:get-rate m-stats)]]
                                 :duration 1500})))))

;; (defn refresh-stats [stats charts]
;;   (reload-stats stats)
;;   (re-chart stats charts))

(defn map-stats [s]
  (for [[k v] (-> s :aggregated :map-stats)] 
    [:div (str k) "-> { get-count: " (:get-count v) 
                     ", put-count: " (:put-count v) 
                     ", get-rate: " (:get-rate v) 
                     ", put-rate: " (:put-rate v) 
                     ", hit-rate: " (:hit-rate v) 
     "}"]))

(defn members [s]
  (map name (-> s :per-node keys)))

(defn- refresh [m-name stats c-div]
  (refresh-stats stats)
  (refresh-top stats @c-div)
  (update-map-area stats m-name @c-div))

(defn refresh-it [m-name stats]
  (let [c (atom {})]
    (js/setInterval #(refresh m-name stats c) refresh-interval)
    (js/setTimeout #(reset! c (charts-for/map-view)) 100))) ;; wait until react renders c3 needed div

(defn show-stats [stats]
  [:div
    [:div [:h6 "members: " (for [m (members @stats)] [:span m " "])]]
    [:div.cpu-usage]
    [:div.map-stats]
    [:div "maps: " (map-stats @stats)]])
