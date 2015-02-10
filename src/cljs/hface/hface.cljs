(ns hface
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [hface.stats :refer [show-stats refresh-it]])
    (:import goog.History))

(defn home-page []
  (let [stats (atom {})]
    [:div [:h3 "hface: look you cluster in the face"]
     [:div [show-stats stats]]
     [:div (refresh-it "appl" stats)]]))
     ;; [:div [:a {:href "#/about"} "about hface"]]])

(defn about-page []
  [:div [:h3 "about hface"]
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
