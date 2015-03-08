(ns hface.dash.component
    (:require [reagent.core :as r]
              [hface.charts :as chart-for]
              [hface.dash.refresh :refer [refresh-interval 
                                          refresh-cpu 
                                          refresh-mem 
                                          refresh-os-mem 
                                          refresh-stats
                                          update-map-area
                                          update-q-area]]
              [hface.stats :refer [members 
                                   map-ops 
                                   map-highlevel
                                   q-ops
                                   q-highlevel]]
              [hface.tools :refer [every by-id]]))

(def stats (r/atom {}))

;; TODO: refactor to a single atom (to be used with Om style cursors)
(def active-map (r/atom {}))
(def active-q (r/atom {}))

(def divs {:cluster-cpu (by-id "cluster-cpu")
           :cluster-memory (by-id "cluster-memory")
           :cluster-area-chart (by-id "cluster-area-chart")
           :cluster-members (by-id "cluster-members")
           :maps (by-id "maps")
           :multi-maps (by-id "multi-maps")
           :queues (by-id "queues")
           :chart-name (by-id "chart-name")})

(every refresh-interval #(refresh-stats stats))

;; to be unmountable. once I figure out how to check if component is mounted to a node, this can go
(def empty-component 
  (fn [] nil)) 

(defn f-to-react 
 "react component needs to return [:div] or nil"
  [f]
  (fn [] (f) nil))

;; components

(defn with-refresh [refresh clazz ui-component]
  (let [div (r/atom nil)
        c (atom nil)
        refresh-it (with-meta (f-to-react #(refresh stats @div))
                              {:component-did-mount (fn [] (let [ui (ui-component clazz)] 
                                                              (reset! c ui)
                                                              (reset! div ui)))
                               :component-will-unmount #(.destroy @c)})]
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

(defn q-stats []
  (with-refresh (partial update-q-area active-q)
                :queue-stats
                chart-for/q-area-chart))

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

(defn hz-queues []
  [:ul.nav.nav-second-level
   (for [q (-> @stats :aggregated :queue-stats keys)]
     ^{:key q} [:li [:a {:href (str "#queues/" (name q))}
                       (name q) [:span.f-right (q-ops q stats :queue-stats) " " [:i.fa.fa-arrow-left]]]])])

(defn map-chart-name []
  (let [{:keys [map-name
                mem 
                entries]} (map-highlevel @active-map stats)]
    [:span (str " " map-name ": entries [" entries "], memory [" mem "]")]))

(defn q-chart-name []
  (let [{:keys [q-name
                mem 
                entries]} (q-highlevel @active-q stats)]
    [:span (str " " q-name ": entries [" entries "], memory [" mem "]")]))

(defn switch-to-chart [c-name c-area 
                       f-name f-area]
    (r/unmount-component-at-node c-area)
    (r/unmount-component-at-node c-name)
    (r/render-component [f-area] c-area)
    (r/render-component [f-name] c-name))

(defn switch-to-map [m t]
  ;; TODO: check if currently the same to noop
  (reset! active-map {:m-name m :m-type t})
  (let [{:keys [cluster-area-chart chart-name]} divs]
    (switch-to-chart chart-name 
                     cluster-area-chart 
                     map-chart-name 
                     map-stats)))

(defn switch-to-q [q t]
  ;; TODO: check if currently the same to noop
  (reset! active-q {:q-name q :q-type t})
  (let [{:keys [cluster-area-chart chart-name]} divs]
    (switch-to-chart chart-name 
                     cluster-area-chart 
                     q-chart-name 
                     q-stats)))

