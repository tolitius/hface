(ns hface.dash.component
    (:require [reagent.core :as reagent]
              [hface.charts :as chart-for]
              [hface.dash.refresh :refer [refresh-interval 
                                          refresh-cpu 
                                          refresh-mem 
                                          refresh-stats
                                          update-map-area]]
              [hface.stats :refer [members map-ops]]
              [hface.tools :refer [every]]))

(def stats (reagent/atom {}))
(def active-map (reagent/atom ""))


(defn switch-to-map [m]                             ;;TODO: refactor this (state dependent) guy out to.. routes?
  ;; TODO: clear the map chart
  (reset! active-map m))

(every refresh-interval #(refresh-stats stats))


;; components

(defn cpu-usage [clazz]
  (let [cpu-div (reagent/atom {})]
    (every refresh-interval #(refresh-cpu stats @cpu-div))
    (js/setTimeout #(reset! cpu-div (chart-for/cpu-gauge clazz)) 100)
    (fn []
      [:div {:class clazz}])))

(defn memory-usage [clazz]
  (let [mem-div (reagent/atom {})]
    (every refresh-interval #(refresh-mem stats @mem-div))
    (js/setTimeout #(reset! mem-div (chart-for/mem-gauge clazz)) 100)
    (fn []
      [:div {:class clazz}])))

(defn map-stats []
  (let [map-stats-div (reagent/atom {})]
    (every refresh-interval #(update-map-area stats active-map @map-stats-div))
    (js/setTimeout #(reset! map-stats-div (chart-for/map-area-chart)) 100)
    (fn []
      [:div.map-stats])))

(defn cluster-members []
  [:ul.nav.nav-second-level
   (for [member (members @stats)]
     ^{:key member} [:li [:a {:href "#"} member]])])

(defn hz-maps []
  [:ul.nav.nav-second-level
   (for [hmap (-> @stats :aggregated :map-stats keys)]
     ^{:key hmap} [:li [:a {:href (str "#maps/" (name hmap))}
                       (name hmap) [:span.f-right (map-ops hmap stats) " " [:i.fa.fa-arrow-left]]]])])

(defn map-chart-name []
  [:span (str " " @active-map " stats")])
