(ns poorsmatic.producer
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str]
            [immutant.daemons :as dmn]
            [poorsmatic.config :as cfg]
            [poorsmatic.twitter :as twitter]))

(defn url-extractor
  "Returns a function that parses a tweet for a URL and, if found,
   invokes handler with it"
  [handler]
  (fn [{text :text}]
    (when-let [url (and text (re-find #"http://[\w/.-]+" text))]
      (log/info text)
      (handler url))))

(defn ^:private make-observer
  "Respond to configuration changes (new search terms)"
  [stream handler]
  (fn [terms]
    (let [filter (str/join "," terms)
          old @stream]
      (log/info "Tweets filter:" filter)
      (reset! stream (if (not-empty terms)
                       (twitter/filter-tweets filter handler)))
      (twitter/close old))))

(defn daemon
  "Start service that filters tweets for urls"
  [handler]
  (let [tweets (atom nil)
        configurator (atom nil)
        extractor (url-extractor handler)]
    (dmn/daemonize
     "tweet-urls"
     (reify dmn/Daemon
       (start [_]
         (log/info "Starting tweets service")
         (reset! configurator (cfg/observe (make-observer tweets extractor)))
         (cfg/notify))
       (stop [_]
         (twitter/close @tweets)
         (cfg/ignore @configurator)
         (log/info "Stopped tweets service"))))))
