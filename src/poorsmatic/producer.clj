(ns poorsmatic.producer
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str]
            [immutant.daemons :as dmn]
            [poorsmatic.config :as cfg]
            [poorsmatic.twitter :as twitter]
            [poorsmatic.models :as model]))

(defn ^:private url-extractor
  "Returns a function that parses a tweet for a URL and, if found,
   invokes handler with it"
  [handler]
  (fn [{text :text}]
    (when-let [url (and text (re-find #"http://[\w/.-]+" text))]
      (log/info text)
      (handler url))))

(defn ^:private make-observer
  "Return a function that responds to configuration changes"
  [stream handler]
  (fn [terms]
    (let [filter (str/join "," terms)]
      (log/info "Tweets filter:" filter)
      (twitter/close @stream)
      (reset! stream (if (not-empty terms)
                       (twitter/filter-tweets filter handler))))))

(defn daemon
  "Start service that filters tweets for urls"
  [handler]
  (let [tweets (atom nil)
        configurator (atom nil)]
    (dmn/daemonize
     "tweet-urls"
     (reify dmn/Daemon
       (start [_]
         (log/info "Starting tweets service")
         (let [callback (make-observer tweets (url-extractor handler))]
           (callback (model/get-all-terms))
           (reset! configurator (cfg/observe callback))))
       (stop [_]
         (cfg/ignore @configurator)
         (twitter/close @tweets)
         (log/info "Stopped tweets service"))))))
