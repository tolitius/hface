(ns hface.stats
    (:require [reagent.core :as reagent :refer [atom]]
              [cognitect.transit :as t]
              [goog.net.XhrIo :as xhr]))

(defonce refresh-interval 1000)

(defn refresh-stats [stats]
  (xhr/send "cluster-stats"
            (fn [event]
              (let [t-reader (t/reader :json)
                    res (-> event .-target .getResponseText)]
                (reset! stats (t/read t-reader res))))
            "GET"))

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

(defn show-stats []
  (let [stats (atom {})]
    (fn []
      (js/setTimeout #(refresh-stats stats) refresh-interval)
      [:div
        [:div [:h6 "members: " (for [m (members @stats)] [:span m " "])]]
        [:div "maps: " (map-stats @stats)]])))
