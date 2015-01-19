(defproject hface "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs" "src/clj/hface"]
  :java-source-paths ["src/java"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.facebook/react "0.11.2"]
                 [reagent "0.4.3"]
                 [monr "0.1.5"]
                 [reagent-utils "0.1.0"]
                 [secretary "1.2.1"]
                 [org.clojure/clojurescript "0.0-2496" :scope "provided"]
                 [com.cemerick/piggieback "0.1.3"]
                 [weasel "0.4.2"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.2"]
                 [prone "0.8.0"]
                 [compojure "1.3.1"]
                 [selmer "0.7.7"]
                 [environ "1.0.0"]
                 [leiningen "2.5.0"]
                 [figwheel "0.2.1-SNAPSHOT"]
                 [com.hazelcast/hazelcast "3.4"]
                 [com.hazelcast/hazelcast-client "3.4"]
                 [com.cognitect/transit-clj "0.8.259"]
                 [com.cognitect/transit-cljs "0.8.199"]
                 [cheshire "5.4.0"]
                 [clj-wallhack "1.0.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/java.data "0.1.1"]]

  :plugins [
            [lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]
            [lein-ring "0.8.13"]
            [lein-asset-minifier "0.2.0"]]

ui.routes {:handler hface.ui.routes/app}

  :min-lein-version "2.5.0"

  :uberjar-name "hface.jar"

  :minify-assets
  {:assets
    {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles {:dev {:repl-options {:init-ns hface.hz
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.2"]
                                  [pjstadig/humane-test-output "0.6.0"]
                                  [org.hface/hface "0.1.0"]]            ;; is needed until figweel bug (https://github.com/bhauman/lein-figwheel/issues/68) is fixed

                   :plugins [[lein-figwheel "0.2.1-SNAPSHOT"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :css-dirs ["resources/public/css"]
                              :ring-handler hface.ui.routes/app}

                   :ring {:handler hface.ui.routes/app}

                   :env {:dev? true}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:source-map true}}}}}

             :uberjar {:hooks [leiningen.cljsbuild minify-assets.plugin/hooks]
                       :env {:production true}
                       :aot :all
                       :omit-source true
                       ;;TODO: figure out how to clean properly
                       ;:prep-tasks [["cljsbuild" "clean"]]
                       :cljsbuild {:jar true
                                   :builds {:app
                                             {:source-paths ["env/prod/cljs"]
                                              :compiler
                                              {:optimizations :advanced
                                               :pretty-print false}}}}}

             :production {:ring {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}}})
