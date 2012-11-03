(ns poorsmatic.config
  (:require [immutant.messaging :as msg]))

(def topic "/topic/config")

(defn observe
  "Register a callback"
  [callback]
  (msg/listen topic callback))

(defn notify
  "Send all observers the new configuration"
  [cfg]
  (msg/publish topic cfg))

(defn dispose
  "De-register the observer"
  [observer]
  (msg/unlisten observer))

(defn start [] (msg/start topic))
(defn stop [] (msg/stop topic))

