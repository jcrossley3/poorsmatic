(defproject poorsmatic "0.1.0-SNAPSHOT"
  :description "A poor man's Prismatic"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [clj-http "0.5.5"]
                 [twitter-api "0.6.12"]
                 [org.clojars.jcrossley3/korma "1.0.0-SNAPSHOT"
                  :exclusions [[org.clojure/java.jdbc]]]
                 [lobos "1.0.0-SNAPSHOT"
                  :exclusions [[org.clojure/java.jdbc]]]
                 [com.h2database/h2 "1.3.160"]
                 [compojure "1.1.3"]
                 [hiccup "1.0.1"]]
  :db-spec {:name "java:jboss/datasources/ExampleDS"
            :subprotocol "h2"}
  :profiles {:dev {:immutant {:swank-port 4005}}
             :prod {:immutant {:init poorsmatic.core/start}
                    :db-spec {:classname "org.h2.Driver"
                              :subprotocol "h2"
                              :subname "file:/tmp/poorsmatic;MVCC=TRUE;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE"}}
             :openshift {:immutant {:init poorsmatic.core/start}
                         :db-spec  {:name "java:jboss/datasources/PostgreSQLDS"
                                    :subprotocol "postgresql"}}})
