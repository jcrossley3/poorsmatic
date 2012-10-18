(ns poorsmatic.core
  (:require [poorsmatic.app :as app]
            [immutant.messaging :as msg]))

(defn start
  "Start up the application's resources"
  []
  (msg/start app/tweets-endpoint)
  (msg/start app/urls-endpoint)
  (app/start))

(defn stop
  "Cleanly shutdown the application's resources "
  []
  (app/stop)
  (msg/stop app/urls-endpoint :force true)
  (msg/stop app/tweets-endpoint :force true))
