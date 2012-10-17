(ns poorsmatic.core
  (:use [poorsmatic.handlers :only [make-url-extractor make-multiword-scraper]])
  (:require [poorsmatic tweets handlers]
            [immutant.messaging :as msg]))

(defn start
  "Start up the application's resources"
  [terms]
  (msg/start "queue/tweets")
  (msg/start "queue/urls")
  (def scraper (msg/listen "queue/urls" (make-multiword-scraper terms)))
  (def url-extractor (msg/listen "queue/tweets" (make-url-extractor "queue/urls")))
  (def daemon (poorsmatic.tweets/daemon terms #(msg/publish "queue/tweets" %))))

(defn stop
  "Cleanly shutdown the application's resources "
  []
  (.stop daemon)
  (msg/unlisten url-extractor)
  (msg/unlisten scraper)
  (msg/stop "queue/urls" :force true)
  (msg/stop "queue/tweets" :force true))
