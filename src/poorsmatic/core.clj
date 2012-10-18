(ns poorsmatic.core
  (:require [poorsmatic [app :as app] [web :as web]]
            [immutant.messaging :as msg]))

(defn start
  "Start up the application's resources"
  []
  (msg/start app/tweets-endpoint)
  (msg/start app/urls-endpoint)
  (web/start)
  (app/start))

(defn stop
  "Cleanly shutdown the application's resources "
  []
  (app/stop)
  (web/stop)
  (msg/stop app/urls-endpoint :force true)
  (msg/stop app/tweets-endpoint :force true))
