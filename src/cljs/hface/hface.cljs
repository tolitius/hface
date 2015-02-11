(ns hface
    (:require [reagent.core :as reagent]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [hface.stats :refer [map-stats cpu-usage memory-usage cluster-members hz-maps switch-to-map map-chart-name]])
    (:import goog.History))


(defn home-page []
    [:div [:h3 "hface: look you cluster in the face"]
     [:div [:a {:href "#/about"} "about hface"]]])

(defn about-page []
  [:div [:h3 "about hface"]
   [:div [:a {:href "#/"} "hface home"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

(defn map-chart [m-name]
  (switch-to-map m-name))

;; -------------------------
;; routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page cpu-usage))

(secretary/defroute "/maps/:map-name" [map-name]
  (session/put! :current-page (map-chart map-name)))

(secretary/defroute "/about" []
  (session/put! :current-page about-page))

;; -------------------------
;; initialize app
(defn init! []
  ;; (reagent/render-component [current-page] (.getElementById js/document "app"))
  (reagent/render-component [cpu-usage :cpu-usage] (.getElementById js/document "cluster-cpu"))
  (reagent/render-component [memory-usage :mem-usage] (.getElementById js/document "cluster-memory"))
  (reagent/render-component [map-stats] (.getElementById js/document "map-area-chart"))
  (reagent/render-component [cluster-members] (.getElementById js/document "cluster-members"))
  (reagent/render-component [hz-maps] (.getElementById js/document "maps"))
  (reagent/render-component [map-chart-name] (.getElementById js/document "map-chart-name")))

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
