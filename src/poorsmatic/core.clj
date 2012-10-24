(ns poorsmatic.core
  (:require [clojure.tools.logging :as log]
            [poorsmatic
             [web :as web]
             [config :as cfg]
             [tweets :as tweet]
             [scrape :as scrape]
             [models :as model]]
            [immutant.messaging :as msg]))

(def tweets "/queue/tweets")
(def urls   "/queue/urls")

(def
  ^{:doc "The running state of the application"
    :private true}
  application (atom nil))

(defn start-application
  "Initialize application's internal state"
  []
  (when-not @application
    (reset! application
            {:url-extractor
             (msg/listen tweets (tweet/url-extractor #(msg/publish urls %)))
             :scraper
             (scrape/start urls)
             :daemon
             (tweet/daemon #(msg/publish tweets %))})))

(defn stop-application
  "Cleanly shutdown the application's internal resources"
  []
  (.stop (:daemon @application))
  (scrape/stop (:scraper @application))
  (msg/unlisten (:url-extractor @application))
  (reset! application nil))

(defn start
  "Start up everything"
  []
  (model/setup-db)
  (msg/start tweets)
  (msg/start urls)
  (cfg/start)
  (web/start)
  (start-application)
  (cfg/notify)
  @application)

(defn stop
  "Cleanly shutdown everything "
  []
  (stop-application)
  (web/stop)
  (cfg/stop)
  (msg/stop urls :force true)
  (msg/stop tweets :force true))
