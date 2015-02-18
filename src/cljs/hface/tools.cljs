(ns hface.tools
  (:require [cljs-time.core :refer [minus plus now seconds]]))

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
                 (plus (now) (seconds 1)))))  ;; starting from 1 second into the future

(defn byte-size [n]
  (js/filesize n))
