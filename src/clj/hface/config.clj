(ns hface.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:import [java.util MissingResourceException]))

(defn resource [path]
  (when path
    (or (-> (Thread/currentThread) .getContextClassLoader (.getResource path)) 
        path)))

(def props
  (if-let [path (or (System/getProperty "hface.conf") (System/getenv "HFACE_CONF"))]
    (try
      (edn/read-string 
        (slurp (io/file (resource path))))
      (catch Exception e 
        (throw (IllegalArgumentException. 
                 (str "a path to hface.conf \"" path "\" can't be found or have an invalid config (problem with the format?) " e)))))
    (throw (MissingResourceException. 
            "hface can't find a \"hface.conf\" env variable that points to a configuration file (usually in a form of -Dhface.conf=<path> or 'export HFACE_CONF=<path>')"
            "" ""))))

(defn conf [& path]                  ;; e.g. (conf :datomic :url)
  (get-in props (vec path)))

;; (defn confa-x [& ps]
;;   (apply conf (concat [:x :y] ps)))
