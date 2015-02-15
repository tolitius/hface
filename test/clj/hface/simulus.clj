(ns hface.simulus
  (:require [hface.hz :refer [cluster-of hz-map]])
  (:refer-clojure :exclude [take])
  (:import [java.util UUID]))

(defn run-in-threads [n f]
  (dotimes [_ n] 
    (.start (Thread. f))))

(defn do-ops [m op n]
  (let [hm (hz-map (name m))
        f (case op
            :put #(dotimes [_ n] (.put hm (rand-int 10000000) 42))
            ;; :put #(dotimes [_ n] (.put hm (UUID/randomUUID) "snapshot of time.. bytes of the past"))
            :get #(dotimes [_ n] (.get hm (rand-int 10000000)))
            #(println "no op: only :get and :put are supported for now"))]
    (run-in-threads 16 f)))

(defn mputs [m]
  (do-ops m :put 100000))

(defn mgets [m]
  (do-ops m :get 100000))

(defn fire-it-up []
  (doall (cluster-of 3))
  (doall (map (comp hz-map name) [:appl :goog :spy :amzn]))
  (mputs :appl)
  (mgets :goog)
  (dotimes [_ 2] (mputs :goog)))
