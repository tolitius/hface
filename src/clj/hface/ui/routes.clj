(ns hface.ui.routes
  (:require [hface.refresh :refer [collect-stats stats]]
            [hface.util :refer [to-transit]]
            [hface.hz :refer [cluster-of]]
            [hface.ui.dev :refer [browser-repl start-figwheel]]
            [clojure.tools.logging :refer [info]]
            [compojure.core :refer [GET] :as c]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [selmer.parser :refer [render-file]]
            [environ.core :refer [env]]
            [prone.middleware :refer [wrap-exceptions]]))

(defn collect-on-start! [collecting?]
  (when-not @collecting?
    (let [interval (or (env :refresh-interval) 1)]
      (info "[hface]: scheduling cluster stats collector to run every " interval " seconds")
      (if (env :dev?) 
        (doall (cluster-of 1)))                                      ;; in dev mode start a one node cluster
      (collect-stats interval)
      (reset! collecting? true))))

(def h-routes
  (let [collecting? (atom false)]
    (c/routes
      (GET "/" [] 
           (collect-on-start! collecting?)                           ;; possibly split UI from collector, then it's not needed here
           (render-file "templates/index.html" {:dev (env :dev?)}))

      (GET "/cluster-stats" [] (to-transit @stats))
      (GET "/stats" [] @stats)
      (resources "/")
      (not-found "Not Found"))))

(def app
  (let [handler (wrap-defaults h-routes site-defaults)]
    (if (env :dev?) (wrap-exceptions handler) handler)))
