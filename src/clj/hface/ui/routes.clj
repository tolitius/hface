(ns hface.ui.routes
  (:require [hface :refer [cluster-stats]]
            [hface.hz :refer [hz-instance]]
            [hface.util.transit :as transit]
            [hface.ui.dev :refer [browser-repl start-figwheel]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [selmer.parser :refer [render-file]]
            [environ.core :refer [env]]
            [prone.middleware :refer [wrap-exceptions]]))

(defroutes routes
  (GET "/" [] (render-file "templates/index.html" {:dev (env :dev?)}))
  (GET "/cluster-stats" [] (transit/write (cluster-stats) :json {}))
  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults routes site-defaults)]
    (hz-instance)
    (if (env :dev?) (wrap-exceptions handler) handler)))
