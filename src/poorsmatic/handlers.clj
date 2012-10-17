(ns poorsmatic.handlers
  (:require [clojure.tools.logging :as log]
            [immutant.messaging :as msg]
            [poorsmatic.scrape :as scrape]))

(defn make-url-extractor
  "Returns a function that parses a tweet for a URL and, if found,
   publishes it to the passed endpoint"
  [endpoint]
  (fn [{tweet :text}]
    (when-let [url (and tweet (re-find #"http:[^\s]*" tweet))]
      (log/info tweet)
      (msg/publish endpoint url))))

(defn make-scraper
  "Returns a function that, given a url, counts the number of words in its content"
  [word]
  (let [count-words-in (scrape/word-counter word)]
    (fn [url]
      (let [count (count-words-in url)]
        (when (> count 0)
          (log/info url ":" word "=>" count))))))

(defn make-multiword-scraper
  "Returns a function that takes a url and counts matches of all
   passed words in its content"
  [words]
  (let [fns (map make-scraper words)]
    (fn [url]
      (doseq [f fns] (f url)))))