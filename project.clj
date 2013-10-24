(defproject poorsmatic "0.1.0-SNAPSHOT"
  :description "A poor man's Prismatic"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [clj-http "0.5.5"]
                 [org.infinispan/infinispan-client-hotrod "6.0.0.CR1"]
                 [com.datomic/datomic-pro "0.8.4159"]
                 [twitter-api "0.6.12"]
                 [compojure "1.1.3"]
                 [hiccup "1.0.1"]]
  :profiles {:prod {:immutant {:init poorsmatic.core/start}}}
  :datomic-uri "datomic:inf://localhost:11222/poorsmatic")
