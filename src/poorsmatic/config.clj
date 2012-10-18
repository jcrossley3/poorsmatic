(ns poorsmatic.config
  (:require [immutant.messaging :as msg]))

(def config-endpoint "/topic/config")

(defn configure
  [v]
  (msg/publish config-endpoint v))

(defn observe
  [f]
  (let [listener (msg/listen config-endpoint f)]
    (with-meta [listener] {:ignore (fn [] (msg/unlisten listener))})))
