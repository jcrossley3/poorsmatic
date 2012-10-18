(ns poorsmatic.config
  (:require [poorsmatic.models :as model]
            [immutant.messaging :as msg]))

(def config-endpoint "/topic/config")

(defn configure
  []
  (msg/publish config-endpoint (model/get-all-terms)))

(defn observe
  [f]
  (msg/listen config-endpoint f))

(defn ignore
  [observer]
  (msg/unlisten observer))

(defn start [] (msg/start config-endpoint))
(defn stop [] (msg/stop config-endpoint))

