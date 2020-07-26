(defproject org.hface/hface-dash "0.1.1-SNAPSHOT"
  :description "look your Hazelcast cluster in the face!"
  :url "https://github.com/tolitius/hface"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]

  :jvm-opts ["-Dconf=./resources/conf/hface.conf"]

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [tolitius/chazel "0.1.23"]
                 [cprop "0.1.7"]
                 [reagent "0.6.0-alpha"]
                 [reagent-utils "0.1.8"]
                 [secretary "1.2.3"]
                 [org.clojure/clojurescript "1.8.51"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.2.0"]
                 [compojure "1.5.0"]
                 [prone "1.1.1"]
                 [selmer "1.0.4"]
                 [com.cognitect/transit-clj "0.8.285"]
                 [com.cognitect/transit-cljs "0.8.237"]
                 [cheshire "5.6.1"]
                 [clj-wallhack "1.0.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/java.data "0.1.1"]]

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-ring "0.9.7"]
            [lein-asset-minifier "0.2.8"]]

  :min-lein-version "2.5.0"

  :uberjar-name "hface-dash.jar"

  :minify-assets
  {:assets
    {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles {:dev {:repl-options {:init-ns hface.stats
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :jvm-opts ["-Xmx4g"]
                   :resource-paths ["test/resources/hz"]
                   :test-paths ["test/clj" "test/cljs"]
                   :source-paths ["env/dev/clj"]

                   :dependencies [[ring/ring-mock "0.3.0"]
                                  [ring/ring-devel "1.4.0"]
                                  [pjstadig/humane-test-output "0.8.0"]
                                  [monr "0.1.6"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [weasel "0.7.0"]
                                  [leiningen "2.5.0"]
                                  [figwheel "0.5.2"]]

                   :plugins [[lein-figwheel "0.5.2"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :css-dirs ["resources/public/css"]
                              :ring-handler hface.ui.routes/app}

                   :ring {:handler hface.ui.routes/app}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:source-map true}}}}}

             ;; creating and executing an "uberjar"
             ;;
             ;; lein do clean, cljsbuild clean, ring uberjar
             ;; java -jar -Dconf=/tmp/hface.conf target/hface-dash.jar

             :uberjar {:hooks [leiningen.cljsbuild minify-assets.plugin/hooks]
                       :aot :all
                       ;; :omit-source true

                       :ring {:handler hface.ui.routes/app}

                       ;;TODO: figure out how to clean properly
                       ;; :prep-tasks [["cljsbuild" "clean"]]
                       :cljsbuild {:jar true
                                   :builds {:app
                                             {:source-paths ["env/prod/cljs"]
                                              :compiler
                                              {:optimizations :whitespace      ;; :advanced breaks C3 js
                                               :pretty-print false}}}}}

             :production {:ring {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}}}

  :scm {:url "https://github.com/tolitius/hface.git"}

  :pom-addition [:developers [:developer {:id "tolitius"}
                             [:name "Anatoly"]
                             [:url "https://github.com/tolitius"]]]

  :repositories {"snapshots" {:url "https://oss.sonatype.org/content/repositories/snapshots/"}}

  :deploy-repositories {"releases" {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                                    :creds :gpg}
                        "snapshots" {:url "https://oss.sonatype.org/content/repositories/snapshots/"
                                     :creds :gpg}})
