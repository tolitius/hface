(ns hface.simulus
  (:require [chazel :refer [cluster-of hz-map hz-mmap hz-queue]])
  (:refer-clojure :exclude [take])
  (:import [java.util UUID]))

(defn run-in-threads [n f]
  (dotimes [_ n] 
    (.start (Thread. f))))

(defn do-ops [m op n]
  (let [f (case op
            :mput #(dotimes [_ n] (.put m (rand-int 10000000) 42))
            :mmput #(dotimes [_ n] (.put m (rand-int 1000000) (rand-int 42)))
            ;; :mput #(dotimes [_ n] (.put hm (UUID/randomUUID) "snapshot of time.. bytes of the past"))
            :mget #(dotimes [_ n] (.get m (rand-int 10000000)))
            :mmget #(dotimes [_ n] (.get m (rand-int 100)))
            #(println "no op: only :get and :put are supported for now"))]
    (run-in-threads 16 f)))

(defn mputs [m]
  (do-ops (hz-map (name m)) :mput 100000))

(defn mgets [m]
  (do-ops (hz-map (name m)) :mget 100000))

(defn mmputs [m]
  (do-ops (hz-mmap (name m)) :mmput 100000))

(defn mmgets [m]
  (do-ops (hz-mmap (name m)) :mmget 100000))

(defn m-gets [m] 
  (dotimes [_ 100000] 
    (.get m (rand-int 100000000))))

(defn m-puts [m] 
  (dotimes [_ 100000] 
    (.put m (rand-int 100000000) 
            (rand-int 10000000))))

(defn q-gets [q] 
  (dotimes [_ 100000] 
    (.poll q)))

(defn q-puts [q] 
  (dotimes [_ 100000] 
    (.put q (rand-int 100000000))))

(defn load-it-up []
  (let [m (hz-map "appl")
        mm (hz-mmap "dow-jones")
        q (hz-queue "quotes")
        fs [#(m-puts m) #(m-puts mm) #(q-puts q) 
            #(m-gets m) #(m-gets mm) #(q-gets q)]]
    (map (partial run-in-threads 8) fs)))

(defn fire-it-up []
  (doall (cluster-of 2))
  (doall (map (comp hz-map name) [:appl :goog :spy :amzn]))
  (mmputs :multi-pulti)
  (mgets :goog)
  (mputs :goog))
