(ns hface.simulus
  (:require [hface.hz :refer [cluster-of hz-map]])
  (:import [java.util UUID]))

(defn nodes-and-maps [& {:keys [nodes maps]}]
  (cluster-of nodes)
  (doall 
    (map (comp hz-map name) maps)))

(defn run-in-threads [n f]
  (dotimes [_ n] 
    (.start (Thread. f))))

(defn do-ops [m op n]
  (let [hm (hz-map (name m))
        f (case op
            :put #(dotimes [_ n] (.put hm (UUID/randomUUID) 42))
            :get #(dotimes [_ n] (.get hm (UUID/randomUUID)))
            #(println "no op: only :get and :put are supported for now"))]
    (run-in-threads 16 f)))

(defn mputs [m]
  (do-ops m :put 100000))

(defn mgets [m]
  (do-ops m :get 100000))

(defn fire-it-up []
  (nodes-and-maps 2 [:appl :goog :spy])
  (mputs :appl)
  (mgets :goog)
  (dotimes [_ 2] (mputs :goog)))
