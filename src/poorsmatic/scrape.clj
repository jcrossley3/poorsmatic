(ns poorsmatic.scrape
  (:use poorsmatic.http)
  (:require [clojure.tools.logging :as log]
            [poorsmatic.models :as model]
            [poorsmatic.config :as cfg]
            [immutant.messaging :as msg]))

(defn make-scraper
  "Returns a function that, given a url, counts the number of words in
   its content, among other things"
  [word]
  (let [scrape (comp (counter word) title url scrape)]
    (fn [url]
      (let [v (scrape url)]
        (log/info word "=>" (:count v))
        (when (> (:count v) 0)
          (model/add-url (assoc v :term word)))
        v))))

(defn make-scrapers
  "Make scrapers for a sequence of words"
  [words]
  (if (empty? words)
    (fn [x])
    (apply juxt (map make-scraper words))))

(defn start
  "Scrape urls looking for words received from the configuration topic"
  [endpoint]
  (let [scraper (atom (fn [x]))
        listener (msg/listen endpoint (fn [url] (@scraper url)) :concurrency 10)
        config (cfg/observe #(reset! scraper (make-scrapers %)))]
    (cfg/notify)
    [listener config]))

(defn stop
  "Cleanly shutdown the return value of start"
  [[listener config]]
  (msg/unlisten listener)
  (cfg/ignore config))