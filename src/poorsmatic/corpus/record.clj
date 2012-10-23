(ns poorsmatic.corpus.record
  (:require [clojure.java.io :as io]
            [poorsmatic.twitter :as twitter]
            [poorsmatic.tweets :as tweets]
            [poorsmatic.http :as http]
            [immutant.messaging :as msg]))

(defn tweet-saver
  [path]
  (fn [tweet]
    (spit (str path "/" (:id_str tweet)) tweet)
    tweet))

(defn url-saver
  [path]
  (fn [m]
    (if-let [url (first (:trace-redirects m))]
      (spit (str path "/" (clj-http.util/url-encode url)) m))
    m))

(defn start
  [path filter]
  (let [tweets (io/file path "tweets")
        urls (io/file path "urls")]
    (.mkdirs tweets)
    (.mkdirs urls)
    (msg/start "queue.tweets")
    (msg/start "queue.urls")
    [(msg/listen "queue.urls" (comp (url-saver urls) http/scrape) :concurrency 10)
     (msg/listen "queue.tweets" (comp (tweets/url-extractor #(msg/publish "queue.urls" %)) (tweet-saver tweets)))
     (twitter/filter-tweets filter #(msg/publish "queue.tweets" %))]))

(defn stop
  [[h1 h2 stream]]
  (twitter/close stream)
  (msg/unlisten h2)
  (msg/unlisten h1)
  (msg/stop "queue.urls" :force true)
  (msg/stop "queue.tweets" :force true))