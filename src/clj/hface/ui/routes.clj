(ns hface.ui.routes
  (:require [hface.stats :refer [cluster-stats]]
            [hface.util :refer [to-transit every]]
            [hface.ui.dev :refer [browser-repl start-figwheel]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [selmer.parser :refer [render-file]]
            [environ.core :refer [env]]
            [prone.middleware :refer [wrap-exceptions]]))

(def stats (atom {}))

(defn refresh-stats [seconds s]
  (every seconds #(reset! s 
                          (to-transit (cluster-stats)))))

(defroutes routes
  (GET "/" [] (render-file "templates/index.html" {:dev (env :dev?)}))
  ;; (GET "/cluster-stats" [] (to-transit (cluster-stats @hz)))
  (GET "/cluster-stats" [] @stats)
  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults routes site-defaults)]
    (refresh-stats 1 stats)
    (if (env :dev?) (wrap-exceptions handler) handler)))
