(ns hface
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [hface.stats :refer [show-stats]])
    (:import goog.History))

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
