(defproject org.hface/hface-client "0.1.0"
  :description "look your Hazelcast cluster in the face!"
  :url "https://github.com/tolitius/hface"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :java-source-paths ["src/java"]

  :dependencies [[chazel "0.1.0"]]

  ;; ceremony for maven central

  :scm {:name "git"
        :url "https://github.com/tolitius/hface.git"}

  :pom-addition [:developers [:developer 
                               [:id "tolitius"]
                               [:name "Anatoly"]
                               [:url "https://github.com/tolitius"]]]

  :deploy-repositories {"releases" {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                                    :creds :gpg}
                        "snapshots" {:url "https://oss.sonatype.org/content/repositories/snapshots/"
                                     :creds :gpg}}
            
  :classifiers {:javadoc {:java-source-paths ^:replace ["src/java"]
                          :source-paths ^:replace []
                          :resource-paths ^:replace []}
                :sources {:java-source-paths ^:replace ["src/java"]
                          :resource-paths ^:replace []}})
