(ns poorsmatic.twitter
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [twitter.oauth :as oauth]
            [twitter.callbacks.handlers :as handler]
            [twitter.api.streaming :as stream]
            [immutant.daemons :as dmn]
            [poorsmatic.config :as cfg]
            [poorsmatic.models :as model])
  (:import twitter.callbacks.protocols.AsyncStreamingCallback))

(def twitter-creds-format "[app-key app-secret user-token user-token-secret]")

(def
  ^{:doc (str "Twitter creds format=" twitter-creds-format)
    :private true}
  twitter-creds (if-let [creds (or (io/resource "twitter-creds")
                                   (io/file "/tmp/twitter-creds"))]
                  (apply oauth/make-oauth-creds (read-string (slurp creds)))
                  (throw (Exception. (str "Missing 'twitter-creds' resource. "
                                          "Required format, including square brackets: "
                                          twitter-creds-format)))))

(defn ^:private handle
  "Process a chunk of async tweetness"
  [handler]
  (fn [response baos]
    (try
      (handler (json/read-json (str baos)))
      (catch Throwable ignored))))

(defn filter-tweets
  "Invoke a twitter-api streaming connection for a comma-delimited
   statuses filter string"
  [filter handler]
  (let [callback (AsyncStreamingCallback.
                  (handle handler)
                  (comp println handler/response-return-everything)
                  handler/exception-print)]
    (stream/statuses-filter :params {:track filter}
                            :oauth-creds twitter-creds
                            :callbacks callback)))

(defn close
  [stream]
  (if stream ((:cancel (meta stream)))))

(defn attach-urls
  "Parses a tweet for a URLs and, if found, adds them to the tweet."
  [{text :text :as tweet}]
  ;(log/info text)
  (assoc tweet
    :content-urls (and text (re-seq #"http://[\w/.-]+" text))))

(defn reconnect
  "Return a function that responds to configuration changes"
  [stream handler]
  (fn [terms]
    (let [filter (str/join "," terms)]
      (log/info "Tweets filter:" filter)
      (close @stream)
      (reset! stream
              (if (not-empty terms)
                (filter-tweets filter handler))))))

(defn daemon
  "Start service that filters tweets for urls"
  [handler]
  (let [tweets (atom nil)
        configurator (atom nil)
        start (fn []
                (log/info "Starting tweets service")
                (let [reconfigure (reconnect tweets handler)]
                  (reconfigure (model/get-terms))
                  (reset! configurator (cfg/observe reconfigure))))
        stop  (fn []
                (cfg/dispose @configurator)
                (close @tweets)
                (log/info "Stopped tweets service"))]
    (dmn/daemonize "tweet-urls" start stop)))
