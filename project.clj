(defproject poorsmatic "0.1.0-SNAPSHOT"
  :description "A poor man's Prismatic"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [twitter-api "0.6.11" :exclusions [org.apache.httpcomponents/httpcore]]
                 [clj-http "0.5.5"]]
  :immutant {:swank-port 4005})
