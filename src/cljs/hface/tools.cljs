(ns hface.tools)

(defn to-css-class 
  "keyword to CSS class. e.g. :chart to '.chart'"
  [k]
  (->> k name (str ".")))
