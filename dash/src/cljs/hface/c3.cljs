(ns hface.c3
  (:require [hface.tools :refer [to-css-class seconds-range]]))

(defn gauge [elem {:keys [data-is init-value height label pattern thresholds]
                   :or   {data-is "usage"
                          init-value 0
                          label {}
                          height 180
                          pattern ["#60B044" "#F6C600" "#F97600" "#FF0000"]
                          thresholds [30 60 90 100]}}]
  (.generate js/c3 (clj->js
                     {:bindto (to-css-class elem)
                      :data
                        {:columns [[data-is init-value]]
                         :type "gauge"}
                      :gauge {:max 100
                              :width 25
                              :label label}
                      :color
                        {:pattern pattern
                         :threshold {:values thresholds
                                     :unit "%"}}
                      :size {:height height}})))

(defn map-area [elem & {:keys [x-span x-label x-format]
                        :or   {x-span (seconds-range 20)
                               x-label "seconds"
                               x-format "%H:%M:%S"}}]
  (let [zeros (replicate (count x-span) 0)]
    ;; (.log js/console (clj->js (cons "x" x-span)))
    (.generate js/c3 (clj->js
                       {:bindto (to-css-class elem)
                        :data
                          {:x "x"
                           :columns [(cons "x" x-span)
                                     (cons "puts" zeros)
                                     (cons "hits" zeros)
                                     (cons "gets" zeros)]
                           :types {:gets "area-spline"
                                   :puts "area-spline"
                                   :hits "area-spline"}
                           :names {:gets "gets/s"
                                   :puts "puts/s"
                                   :hits "total hits/s"}}
                        :axis {:y {:label "ops/s"}
                               :x
                                 {:label x-label
                                  :type "timeseries"
                                  :tick {:format x-format}
                                  ;; :ticks 20
                                  }}}))))

(defn q-area [elem & {:keys [x-span x-label x-format]    ;; only _maybe_ combine with map-area
                      :or   {x-span (seconds-range 20)
                             x-label "seconds"
                             x-format "%H:%M:%S"}}]
  (let [zeros (replicate (count x-span) 0)]
    ;; (.log js/console (clj->js (cons "x" x-span)))
    (.generate js/c3 (clj->js
                       {:bindto (to-css-class elem)
                        :data
                          {:x "x"
                           :columns [(cons "x" x-span)
                                     (cons "puts" zeros)
                                     (cons "rejected-puts" zeros)
                                     (cons "takes" zeros)
                                     (cons "empty-takes" zeros)]
                           :types {:puts "area-spline"
                                   :rejected-puts "area-spline"
                                   :takes "area-spline"
                                   :empty-takes "area-spline"}
                           :names {:puts "puts/s"
                                   :rejected-puts "rejected puts/s"
                                   :takes "takes/s"
                                   :empty-takes "empty takes/s"}}
                        :axis {:y "ops / s"
                               :x
                                 {:label x-label
                                  :type "timeseries"
                                  :tick {:format x-format}
                                  }}}))))
