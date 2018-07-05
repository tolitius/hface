(ns hface.app
    (:require [reagent.core :as reagent]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [hface.dash.component :refer [divs
                                            cpu-usage
                                            memory-usage
                                            dash-name
                                            cluster-members
                                            hz-maps
                                            hz-mmaps
                                            hz-queues
                                            empty-component
                                            switch-to-map
                                            switch-to-q]])
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
  (let [components {:cluster-cpu cpu-usage
                    :cluster-memory memory-usage
                    :cluster-members cluster-members
                    :cluster-name dash-name
                    :maps hz-maps
                    :multi-maps hz-mmaps
                    :queues hz-queues
                    :cluster-area-chart empty-component
                    :chart-name empty-component}]
    (doall
      (for [[el component] components]
        (reagent/render-component [component] (el divs))))))

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
