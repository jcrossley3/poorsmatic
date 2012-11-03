(defproject poorsmatic "0.1.0-SNAPSHOT"
  :description "A poor man's Prismatic"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.immutant/immutant "0.6.0-SNAPSHOT"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.clojars.jcrossley3/poorpus "0.1.0"]
                 [compojure "1.1.3"]
                 [com.datomic/datomic-free "0.8.3551"
                  :exclusions [org.apache.httpcomponents/httpclient]]
                 [hiccup "1.0.1"]]
  :profiles {:dev  {:immutant {:swank-port 4005}
                    :datomic-uri "datomic:mem://poorsmatic"}
             :prod {:immutant {:init poorsmatic.core/start}
                    :datomic-uri "datomic:free://localhost:4334/poorsmatic"}})
