(ns poorsmatic.tweets
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str]
            [immutant.daemons :as dmn]
            [poorsmatic.config :as config]
            [poorsmatic.twitter :as twitter]))

(defn ^:private make-observer
  [stream handler]
  (fn [terms]
    (let [filter (str/join "," terms)]
      (twitter/close @stream)
      (log/info "Tweets filter:" filter)
      (if (empty? terms)
        (reset! stream nil)
        (reset! stream (twitter/filter-tweets filter handler))))))

(defn daemon
  "Start the tweets service"
  [handler]
  (let [tweets (atom nil)
        configurator (atom nil)]
    (dmn/daemonize "tweets"
                   (reify dmn/Daemon
                     (start [_]
                       (log/info "Starting tweets service")
                       (reset! configurator (config/observe (make-observer tweets handler)))
                       (config/configure))
                     (stop [_]
                       (twitter/close @tweets)
                       (config/ignore @configurator)
                       (log/info "Stopped tweets service"))))))

