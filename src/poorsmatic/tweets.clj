(ns poorsmatic.tweets
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [immutant.daemons :as dmn]
            [poorsmatic.config :as config]
            [twitter.oauth :as oauth]
            [twitter.callbacks.handlers :as handler]
            [twitter.api.streaming :as stream]
            [http.async.client :as ac])
  (:import twitter.callbacks.protocols.AsyncStreamingCallback))

(def
  ^{:doc "twitter oauth credentials"
    :private true}
  creds (apply oauth/make-oauth-creds (read-string (slurp "/tmp/creds"))))

(defn ^:private handle
  "Process a chunk of async tweetness"
  [handler]
  (fn [response baos]
    (try
      (handler (json/read-json (str baos)))
      (catch Throwable ignored))))

(defn ^:private filter-tweets
  "Invoke a twitter-api streaming connection for a comma-delimited statuses filter string"
  [filter handler]
  (let [callback (AsyncStreamingCallback. (handle handler)
                                          (comp println handler/response-return-everything)
                                          handler/exception-print)]
    (stream/statuses-filter :params {:track filter}
                            :client (ac/create-client :request-timeout -1) ; TODO: not this
                            :oauth-creds creds
                            :callbacks callback)))
(defn ^:private close
  [stream]
  (if stream ((:cancel (meta stream)))))

(defn ^:private make-observer
  [stream handler]
  (fn [terms]
    (let [filter (str/join "," terms)]
      (close @stream)
      (log/info "Tweets filter:" filter)
      (if (empty? terms)
        (reset! stream nil)
        (reset! stream (filter-tweets filter handler))))))

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
                       (close @tweets)
                       (config/ignore @configurator)
                       (log/info "Stopped tweets service"))))))

