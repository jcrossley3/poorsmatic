(ns poorsmatic.config
  (:require [immutant.messaging :as msg]))

(def config-endpoint "/topic/config")

(defn configure
  [v]
  (msg/publish config-endpoint v))

(defn observe
  [f]
  (msg/listen config-endpoint f))

(defn ignore
  [observer]
  (msg/unlisten observer))