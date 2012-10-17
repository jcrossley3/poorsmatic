(ns poorsmatic.core
  (:use [poorsmatic.handlers :only [make-url-extractor]])
  (:require [poorsmatic tweets handlers]
            [immutant.messaging :as msg]))

(defn start
  "Start up the application's resources"
  [terms]
  (msg/start "queue/tweets")
  (msg/start "queue/urls")
  (def daemon (poorsmatic.tweets/daemon terms #(msg/publish "queue/tweets" %)))
  (def listener (msg/listen "queue/tweets" (make-url-extractor "queue/urls"))))

(defn stop
  "Cleanly shutdown the application's resources "
  []
  (.stop daemon)
  (msg/unlisten listener)
  (msg/stop "queue/urls" :force true)
  (msg/stop "queue/tweets" :force true))
