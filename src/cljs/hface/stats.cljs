(ns hface.stats
    (:require [reagent.core :as reagent :refer [atom]]
              [cognitect.transit :as t]
              [goog.net.XhrIo :as xhr]))

(defn refresh-stats [stats]
  (xhr/send "cluster-stats"
            (fn [event]
              (let [t-reader (t/reader :json)
                    res (-> event .-target .getResponseText)]
                (reset! stats (t/read t-reader res))))
            "GET"))

(defn map-stats [s]
  (for [[k v] (-> s :aggregated :map-stats)] 
    [:div (str k) "-> { get-count: " (:get-count v) ", put-count: " (:put-count v) "}"]))

(defn show-stats []
  (let [stats (atom {})]
    (fn []
      (js/setTimeout #(refresh-stats stats) 1000)
      [:div "maps: " (map-stats @stats)])))
