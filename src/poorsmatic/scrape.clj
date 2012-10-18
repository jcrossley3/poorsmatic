(ns poorsmatic.scrape
  (:refer-clojure :exclude [count])
  (:require [clj-http.client :as client]
            [clojure.string :as s]
            [clojure.tools.logging :as log]
            [poorsmatic.models :as model]
            [poorsmatic.config :as cfg]
            [immutant.messaging :as msg]
            [immutant.cache :as cache]))

(defn scrape [url]
  (try
    (log/info "Fetching" url)
    (time (client/get url {:socket-timeout 10000 :conn-timeout 10000}))
    (catch Exception e {})))
(def memoized-scrape (cache/memo scrape "scraped" :idle 10))

(defn count [word text]
  (-> (s/lower-case (or text ""))
      (s/split #"[^\w]+")
      frequencies
      (get (s/lower-case word) 0)))

(defn word-counter
  "Returns a function that takes a url and returns the number of
   matches for 'word' in its content"
  [word]
  (comp (partial count word) :body memoized-scrape))

(defn make-scraper
  "Returns a function that, given a url, counts the number of words in its content"
  [word]
  (let [count-words-in (word-counter word)]
    (fn [url]
      (let [count (count-words-in url)]
        (when (> count 0)
          (log/info url ":" word "=>" count)
          (model/add-url {:url url, :term word, :count count}))
        count))))

(defn make-robust-scraper
  [words]
  (if (empty? words)
    (fn [x])
    (apply juxt (map make-scraper words))))

(defn start
  [endpoint]
  (let [scraper (atom (fn [x]))
        listener (msg/listen endpoint (fn [url] (@scraper url)))
        config (cfg/observe #(reset! scraper (make-robust-scraper %)))]
    [listener config]))

(defn stop
  [[listener config]]
  (msg/unlisten listener)
  (cfg/ignore config))