(ns hface.dash.component
    (:require [reagent.core :as r]
              [hface.charts :as chart-for]
              [hface.dash.refresh :refer [refresh-interval 
                                          refresh-cpu 
                                          refresh-mem 
                                          refresh-os-mem 
                                          refresh-stats
                                          update-map-area]]
              [hface.stats :refer [members map-ops map-highlevel]]
              [hface.tools :refer [every]]))

(def stats (r/atom {}))
(def active-map (r/atom {}))


(defn switch-to-map [m t]                             ;;TODO: refactor this (state dependent) guy out to.. routes?
  ;; TODO: clear the map chart
  (reset! active-map {:m-name m :m-type t}))

(every refresh-interval #(refresh-stats stats))

(defn f-to-react 
 "react conponent needs to return [:div] or nil"
  [f]
  (fn [] (f) nil))

;; components

(defn with-refresh [refresh clazz ui-component]
  (let [div (r/atom nil)
        refresh-it (with-meta (f-to-react #(refresh stats @div))
                              {:component-did-mount #(reset! div (ui-component clazz))})]
    (fn []
      [:div {:class clazz} 
        [refresh-it]])))

(defn cpu-usage [] 
  (with-refresh refresh-cpu 
                :cpu-usage 
                chart-for/cpu-gauge))

(defn memory-usage []
  (with-refresh refresh-os-mem
                :mem-usage
                chart-for/mem-gauge))

(defn map-stats []
  (with-refresh (partial update-map-area active-map)
                :map-stats
                chart-for/map-area-chart))

(defn cluster-members []
  [:ul.nav.nav-second-level
   (for [member (members @stats)]
     ^{:key member} [:li [:a {:href "#"} member]])])

(defn hz-maps []
  [:ul.nav.nav-second-level
   (for [hmap (-> @stats :aggregated :map-stats keys)]
     ^{:key hmap} [:li [:a {:href (str "#maps/" (name hmap))}
                       (name hmap) [:span.f-right (map-ops hmap stats :map-stats) " " [:i.fa.fa-arrow-left]]]])])

(defn hz-mmaps []
  [:ul.nav.nav-second-level
   (for [hmap (-> @stats :aggregated :multi-map-stats keys)]
     ^{:key hmap} [:li [:a {:href (str "#mmaps/" (name hmap))}
                       (name hmap) [:span.f-right (map-ops hmap stats :multi-map-stats) " " [:i.fa.fa-arrow-left]]]])])

(defn map-chart-name []
  (let [{:keys [map-name
                mem 
                entries]} (map-highlevel @active-map stats)]
    [:span (str " " map-name ": entries [" entries "], memory [" mem "]")]))
