(defproject org.hface/hface-dash "0.1.0-SNAPSHOT"
  :description "look your Hazelcast cluster in the face!"
  :url "https://github.com/tolitius/hface"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]

  :jvm-opts ["-Dconf=./resources/conf/hface.conf"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [chazel "0.1.2-SNAPSHOT"]
                 [cprop "0.1.0"]
                 [com.gitpod/hface-client "0.1.0"]
                 [com.facebook/react "0.11.2"]
                 [reagent "0.4.3"]
                 [reagent-utils "0.1.0"]
                 [secretary "1.2.1"]
                 [org.clojure/clojurescript "0.0-2850" :scope "provided"]
                 [com.andrewmcveigh/cljs-time "0.3.2"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.2"]
                 [compojure "1.3.1"]
                 [prone "0.8.0"]
                 [selmer "0.7.7"]
                 [environ "1.0.0"]
                 [com.cognitect/transit-clj "0.8.259"]
                 [com.cognitect/transit-cljs "0.8.199"]
                 [cheshire "5.4.0"]
                 [clj-wallhack "1.0.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/java.data "0.1.1"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]
            [lein-ring "0.8.13"]
            [lein-asset-minifier "0.2.0"]]

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

                   :dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.2"]
                                  [pjstadig/humane-test-output "0.6.0"]
                                  [monr "0.1.5"]
                                  [com.cemerick/piggieback "0.1.5"]
                                  [weasel "0.6.0-SNAPSHOT"]
                                  [leiningen "2.5.0"]
                                  [figwheel "0.2.5-SNAPSHOT"]]

                   :plugins [[lein-figwheel "0.2.3-SNAPSHOT"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :css-dirs ["resources/public/css"]
                              :ring-handler hface.ui.routes/app}

                   :ring {:handler hface.ui.routes/app}

                   :env {:dev? true
                         :refresh-interval 4}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:source-map true}}}}}

             ;; creating and executing an "uberjar"
             ;;
             ;; lein do clean, cljsbuild clean, ring uberjar
             ;; java -jar -Dconf=/tmp/hface.conf target/hface-dash.jar

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
