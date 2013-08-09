(defproject poorsmatic "0.1.0-SNAPSHOT"
  :description "A poor man's Prismatic"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [clj-http "0.5.5"]
                 [twitter-api "0.6.12"]
                 [org.clojure/java.jdbc "0.3.0-alpha4"]
                 [korma "0.3.0-RC5"]
                 [lobos "1.0.0-beta1"]
                 [com.h2database/h2 "1.3.160"]
                 [compojure "1.1.3"]
                 [hiccup "1.0.1"]]
  :profiles {:dev {:db-spec {:database "mem:poo"
                             :adapter "h2"}}
             :prod {:immutant {:init poorsmatic.core/start}
                    :db-spec {:classname "org.h2.Driver"
                              :subprotocol "h2"
                              :subname "file:/tmp/poorsmatic;MVCC=TRUE;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE"}}
             :openshift {:immutant {:init poorsmatic.core/start}
                         :db-spec  {:name "java:jboss/datasources/PostgreSQLDS"
                                    :subprotocol "postgresql"}}})
