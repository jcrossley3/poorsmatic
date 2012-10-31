(ns poorsmatic.config
  (:require [immutant.messaging :as msg]))

(def topic "/topic/config")

(defn notify
  [v]
  (msg/publish topic v))

(defn observe
  [f]
  (msg/listen topic f))

(defn ignore
  [observer]
  (msg/unlisten observer))

(defn start [] (msg/start topic))
(defn stop [] (msg/stop topic))

