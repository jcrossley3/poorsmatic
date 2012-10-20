(ns poorsmatic.scrape
  (:use poorsmatic.http)
  (:require [clojure.tools.logging :as log]
            [poorsmatic.models :as model]
            [poorsmatic.config :as cfg]
            [immutant.messaging :as msg]))

(defn make-scraper
  "Returns a function that, given a url, counts the number of words in its content"
  [word]
  (let [scrape (comp (counter word) title url scrape)]
    (fn [url]
      (let [v (scrape url)]
        (log/info word "=>" (:count v) (str "\"" (:title v) "\""))
        (when (> (:count v) 0)
          (model/add-url (assoc v :term word)))
        v))))

(defn make-scrapers
  [words]
  (if (empty? words)
    (fn [x])
    (apply juxt (map make-scraper words))))

(defn start
  [endpoint]
  (let [scraper (atom (fn [x]))
        listener (msg/listen endpoint (fn [url] (@scraper url)) :concurrency 10)
        config (cfg/observe #(reset! scraper (make-scrapers %)))]
    [listener config]))

(defn stop
  [[listener config]]
  (msg/unlisten listener)
  (cfg/ignore config))