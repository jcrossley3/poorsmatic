(ns poorsmatic.tweets
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [immutant.daemons :as dmn]
            [twitter.oauth :as oauth]
            [twitter.callbacks.handlers :as handler]
            [twitter.api.streaming :as stream]
            [http.async.client :as ac])
  (:import twitter.callbacks.protocols.AsyncStreamingCallback))

(def
  ^{:doc "twitter oauth credentials"
    :private true}
  creds (apply oauth/make-oauth-creds (read-string (slurp "/tmp/creds"))))

(defn handle
  "Process a chunk of async tweetness"
  [handler]
  (fn [response baos]
    (try
      (handler (json/read-json (str baos)))
      (catch Throwable ignored))))

(defn filter-tweets
  "Invoke a twitter-api streaming connection for a statuses filter"
  [search handler]
  (let [callback (AsyncStreamingCallback. (handle handler)
                                          (comp println handler/response-return-everything)
                                          handler/exception-print)]
    (stream/statuses-filter :params {:track search}
                            :client (ac/create-client :request-timeout -1) ; TODO: not this
                            :oauth-creds creds
                            :callbacks callback)))

(defn daemon
  "Start the tweets service"
  [terms handler]
  (let [search (str/join "," terms)
        tweets (atom nil)]
    (dmn/daemonize "tweets"
                   (reify dmn/Daemon
                     (start [_]
                       (log/info "Starting search:" search)
                       (reset! tweets (filter-tweets search handler)))
                     (stop [_]
                       ((:cancel (meta @tweets)))
                       (log/info "Stopped search:" search))))))

