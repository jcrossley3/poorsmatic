(ns poorsmatic.core
  (:require [clojure.tools.logging :as log]
            [poorsmatic
             [web :as web]
             [config :as cfg]
             [tweets :as tweet]
             [scrape :as scrape]]
            [immutant.messaging :as msg]))

(def tweets-endpoint "/queue/tweets")
(def urls-endpoint   "/queue/urls")

(def
  ^{:doc "The running state of the application"
    :private true}
  application (atom nil))

(defn ^:private url-extractor
  "Parses a tweet for a URL and, if found, publishes it to the
   urls-endpoint"
  [{tweet :text}]
  (when-let [url (and tweet (re-find #"http:[^\s]*" tweet))]
    (log/info tweet)
    (msg/publish urls-endpoint url)))

(defn start-app
  "Initialize application's internal state"
  []
  (when-not @application
    (reset! application
            {:url-extractor
             (msg/listen tweets-endpoint url-extractor)
             :scraper
             (scrape/start urls-endpoint)
             :daemon
             (tweet/daemon #(msg/publish tweets-endpoint %))})))

(defn stop-app
  "Cleanly shutdown the application's internal resources"
  []
  (.stop (:daemon @application))
  (scrape/stop (:scraper @application))
  (msg/unlisten (:url-extractor @application))
  (reset! application nil))

(defn start
  "Start up everything"
  []
  (msg/start tweets-endpoint)
  (msg/start urls-endpoint)
  (cfg/start)
  (web/start)
  (start-app)
  (cfg/configure))

(defn stop
  "Cleanly shutdown everything "
  []
  (stop-app)
  (web/stop)
  (cfg/stop)
  (msg/stop urls-endpoint :force true)
  (msg/stop tweets-endpoint :force true))
