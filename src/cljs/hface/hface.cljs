(ns hface.app
    (:require [reagent.core :as reagent]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [hface.dash.component :refer [map-stats 
                                            q-stats
                                            cpu-usage 
                                            memory-usage 
                                            cluster-members 
                                            hz-maps 
                                            hz-mmaps 
                                            hz-queues 
                                            switch-to-map 
                                            switch-to-q
                                            map-chart-name
                                            q-chart-name]])
    (:import goog.History))

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page cpu-usage))

(secretary/defroute "/maps/:map-name" [map-name]
  (session/put! :current-page (switch-to-map map-name :map-stats)))

(secretary/defroute "/mmaps/:map-name" [map-name]
  (session/put! :current-page (switch-to-map map-name :multi-map-stats)))

(secretary/defroute "/queues/:q-name" [q-name]
  (session/put! :current-page (switch-to-q q-name :queue-stats)))

;; -------------------------
;; initialize app
(defn init! []
  ;; (reagent/render-component [current-page] (.getElementById js/document "app"))
  (reagent/render-component [cpu-usage] (.getElementById js/document "cluster-cpu"))
  (reagent/render-component [memory-usage] (.getElementById js/document "cluster-memory"))
  (reagent/render-component [map-stats] (.getElementById js/document "cluster-area-chart"))
  ;; (reagent/render-component [q-stats] (.getElementById js/document "cluster-area-chart"))
  (reagent/render-component [cluster-members] (.getElementById js/document "cluster-members"))
  (reagent/render-component [hz-maps] (.getElementById js/document "maps"))
  (reagent/render-component [hz-mmaps] (.getElementById js/document "multi-maps"))
  (reagent/render-component [hz-queues] (.getElementById js/document "queues"))
  (reagent/render-component [map-chart-name] (.getElementById js/document "chart-name"))
  (reagent/render-component [q-chart-name] (.getElementById js/document "chart-name")))

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
