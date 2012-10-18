(ns poorsmatic.core
  (:require [poorsmatic
             [app :as app]
             [web :as web]
             [config :as cfg]]
            [immutant.messaging :as msg]))

(defn start
  "Start up the application's resources"
  []
  (msg/start cfg/config-endpoint)
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
  (msg/stop app/tweets-endpoint :force true)
  (msg/stop cfg/config-endpoint :force true))
