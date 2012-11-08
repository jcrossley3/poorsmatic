(ns poorsmatic.twitter
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str]
            [immutant.daemons :as dmn]
            [poorsmatic.config :as cfg]
            [poorsmatic.models :as model]
            [poorpus.twitter :as twitter]))

(defn url-extractor
  "Returns a function that parses a tweet for a URL and, if found,
   invokes handler with it"
  [handler]
  (fn [{text :text}]
    (when-let [url (and text (re-find #"http://[\w/.-]+" text))]
      (log/info text)
      (handler url))))

(defn reconnect
  "Return a function that responds to configuration changes"
  [stream handler]
  (fn [terms]
    (let [filter (str/join "," terms)]
      (log/info "Tweets filter:" filter)
      (twitter/close @stream)
      (reset! stream
              (if (not-empty terms)
                (twitter/filter-tweets filter handler))))))

(defn daemon
  "Start service that filters tweets for urls"
  [handler]
  (let [tweets (atom nil)
        configurator (atom nil)
        start (fn []
                (log/info "Starting tweets service")
                (let [reconfigure (reconnect tweets (url-extractor handler))]
                  (reconfigure (model/get-terms))
                  (reset! configurator (cfg/observe reconfigure))))
        stop  (fn []
                (cfg/dispose @configurator)
                (twitter/close @tweets)
                (log/info "Stopped tweets service"))]
    (dmn/daemonize "tweet-urls" start stop)))
