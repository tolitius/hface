(ns hface.c3.gauge)

(defn to-css-class 
  "keyword to CSS class. e.g. :chart to '.chart'"
  [k]
  (->> k name (str ".")))

(defn generate [elem {:keys [init-value height]
                      :or   {init-value 0
                          height 180}}]
  (.generate js/c3 (clj->js 
                     {:bindto (to-css-class elem)
                      :data 
                      {:columns [["data" init-value]]
                       :type "gauge"}
                      :gauge {}
                      :color 
                      {:pattern (reverse ["#FF0000" "#F97600" "#F6C600" "#60B044"])
                       :threshold {:values [30 60 90 100]}}
                      :size {:height height}})))
