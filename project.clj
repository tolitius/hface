(defproject hface "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]
  ;; :keep-non-project-classes true
  :java-source-paths ["src/java"]

  :jvm-opts ["-Dconf=./resources/conf/hface.conf"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [chazel "0.1.0-SNAPSHOT"]
                 [cprop "0.1.0-SNAPSHOT"]
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

  :plugins [
            [lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]
            [lein-ring "0.8.13"]
            [lein-asset-minifier "0.2.0"]]

ui.routes {:handler hface.ui.routes/app}

  :min-lein-version "2.5.0"

  ;; uncomment to build a hface-client jar. otherwise exclusions would apply to uberjar          
  ;; :jar-exclusions [#"(clj)|(public)|(sample)|(dash)|(conf)|(templates)|(ui)"]

  :jar-name "hface-client.jar"
            
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

  :profiles {:dev {:repl-options {:init-ns stats
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
                                  [figwheel "0.2.5-SNAPSHOT"]
                                  [org.hface/hface-client "0.1.0"]]            ;; is needed until figweel bug (https://github.com/bhauman/lein-figwheel/issues/68) is fixed

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
             ;; lein do cljsbuild clean, ring uberjar
             ;; java -jar -Dconf=/tmp/hface.conf target/hface.jar

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
                                              {:optimizations :whitespace      ;; :advenced breaks C3 js
                                               :pretty-print false}}}}}

             :production {:ring {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}}})
