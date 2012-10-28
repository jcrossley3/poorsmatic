(defproject poorsmatic "0.1.0-SNAPSHOT"
  :description "A poor man's Prismatic"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.immutant/immutant "0.5.0"]
                 [clj-http "0.5.5"]
                 [com.datomic/datomic-free "0.8.3551"]
                 [twitter-api "0.6.11"]
                 [compojure "1.1.3"]
                 [hiccup "1.0.1"]]
  :profiles {:dev {:immutant {:swank-port 4005}}
             :prod {:immutant {:init poorsmatic.core/start}}})
