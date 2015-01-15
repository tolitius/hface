(ns hface.ui.routes
  (:require [hface.stats :refer [cluster-stats]]
            [hface.hz :refer [client-instance]]
            [hface.util :refer [to-transit]]
            [hface.ui.dev :refer [browser-repl start-figwheel]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [selmer.parser :refer [render-file]]
            [environ.core :refer [env]]
            [prone.middleware :refer [wrap-exceptions]]))

(def hz 
  (delay (client-instance)))

(defroutes routes
  (GET "/" [] (render-file "templates/index.html" {:dev (env :dev?)}))
  (GET "/cluster-stats" [] (to-transit (cluster-stats hz)))
  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults routes site-defaults)]
    (if (env :dev?) (wrap-exceptions handler) handler)))
