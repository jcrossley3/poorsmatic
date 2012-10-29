(ns poorsmatic.core
  (:require [poorsmatic
             [web :as web]
             [config :as cfg]
             [producer :as producer]
             [consumer :as consumer]]
            [immutant.messaging :as msg]))

(def urls "/queue/urls")

(defn start
  "Start up everything"
  []
  (msg/start urls)
  (cfg/start)
  (web/start)
  {:scraper
   (consumer/start urls)
   :daemon
   (producer/daemon #(msg/publish urls %))})

(defn stop
  "Cleanly shutdown everything "
  [& {:keys [scraper daemon]}]
  (if daemon (.stop daemon))
  (if scraper (consumer/stop scraper))
  (web/stop)
  (cfg/stop)
  (msg/stop urls :force true))
