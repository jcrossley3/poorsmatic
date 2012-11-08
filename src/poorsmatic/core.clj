(ns poorsmatic.core
  (:require [poorsmatic
             [web :as web]
             [twitter :as producer]
             [scraper :as consumer]]
            [immutant.messaging :as msg]))

(def urls "/queue/urls")
(def application (atom nil))

(defn start
  "Start up everything"
  []
  (when-not @application
    (msg/start urls)
    (web/start)
    (reset! application
            {:scraper
             (consumer/start urls)
             :daemon
             (producer/daemon #(msg/publish urls %))})))

(defn stop
  "Cleanly shutdown everything "
  []
  (when @application
    (.stop (:daemon @application))
    (consumer/stop (:scraper @application))
    (web/stop)
    (msg/stop urls)
    (reset! application nil)))
