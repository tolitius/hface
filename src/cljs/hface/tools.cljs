(ns hface.tools
  (:require [cljs-time.core :refer [minus now seconds]]))

(defn to-css-class 
  "keyword to CSS class. e.g. :chart to '.chart'"
  [k]
  (->> k name (str ".")))

(defn every [ms f]
  (js/setInterval f ms))

(defn info [& m]
  (.log js/console (clj->js m)))

(defn seconds-range [n]
  (take n 
        (iterate #(minus % (seconds 1)) 
                 (now))))
