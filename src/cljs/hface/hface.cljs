(ns hface
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [cognitect.transit :as t]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.net.XhrIo :as xhr]
              [goog.history.EventType :as EventType])
    (:import goog.History))

(defn refresh-stats [stats]
  (xhr/send "cluster-stats"
            (fn [event]
              (let [t-reader (t/reader :json)
                    res (-> event .-target .getResponseText)]
                (reset! stats (t/read t-reader res))))
            "GET"))

;; -------------------------
;; views

(defn map-stats [s]
  (for [[k v] (-> s :aggregated :map-stats)] 
    [:div (str k) "-> { get-count: " (:get-count v) ", put-count: " (:put-count v) "}"]))

(defn show-stats []
  (let [stats (atom {})]
    (fn []
      (js/setTimeout #(refresh-stats stats) 1000)
      [:div "maps: " (map-stats @stats)])))

(defn home-page []
  [:div [:h2 "welcome to hface"]
   [:div [show-stats]]
   [:div [:a {:href "#/about"} "about hface"]]])

(defn about-page []
  [:div [:h2 "about hface"]
   [:div [:a {:href "#/"} "hface home"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page home-page))

(secretary/defroute "/about" []
  (session/put! :current-page about-page))

;; -------------------------
;; initialize app
(defn init! []
  (reagent/render-component [current-page] (.getElementById js/document "app")))

;; -------------------------
;; history
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))
;; need to run this after routes have been defined
(hook-browser-navigation!)
