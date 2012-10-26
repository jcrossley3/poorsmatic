(ns poorsmatic.tweets
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str]
            [immutant.daemons :as dmn]
            [poorsmatic.config :as config]
            [poorsmatic.twitter :as twitter]))

(defn url-extractor
  "Returns a function that parses a tweet for a URL and, if found,
   invokes handler with it"
  [handler]
  (fn [{text :text :as tweet}]
    (when-let [url (and text (re-find #"http://[\w/.-]+" text))]
      (log/info text)
      (handler url))))

(defn ^:private make-observer
  [stream handler]
  (fn [terms]
    (let [filter (str/join "," terms)
          old @stream]
      (log/info "Tweets filter:" filter)
      (reset! stream (if (not-empty terms)
                       (twitter/filter-tweets filter handler)))
      (twitter/close old))))

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
                       (config/notify))
                     (stop [_]
                       (twitter/close @tweets)
                       (config/ignore @configurator)
                       (log/info "Stopped tweets service"))))))

