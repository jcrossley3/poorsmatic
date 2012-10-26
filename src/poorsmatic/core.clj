(ns poorsmatic.core
  (:require [poorsmatic
             [web :as web]
             [config :as cfg]
             [tweets :as tweet]
             [scrape :as scrape]]
            [immutant.messaging :as msg]))

(def urls "/queue/urls")

(defn start
  "Start up everything"
  []
  (msg/start urls)
  (cfg/start)
  (web/start)
  {:scraper
   (scrape/start urls)
   :daemon
   (tweet/daemon #(msg/publish urls %))})

(defn stop
  "Cleanly shutdown everything "
  [{:keys [scraper daemon]}]
  (.stop daemon)
  (scrape/stop scraper)
  (web/stop)
  (cfg/stop)
  (msg/stop urls :force true))
