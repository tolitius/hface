(ns hface.c3.gauge)

(defn generate [& {:keys [init-value height]
                   :or   {init-value 0
                          height 180}}]
  (.generate js/c3 (clj->js 
                     {:data 
                      {:columns [["data" init-value]]
                       :type "gauge"}
                      :gauge {}
                      :color 
                      {:pattern (reverse ["#FF0000" "#F97600" "#F6C600" "#60B044"])
                       :threshold {:values [30 60 90 100]}}
                      :size {:height height}})))
