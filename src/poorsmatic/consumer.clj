(ns poorsmatic.consumer
  (:use poorsmatic.scraper)
  (:require [clojure.tools.logging :as log]
            [poorsmatic.models :as model]
            [poorsmatic.config :as cfg]
            [immutant.messaging :as msg]))

(defn make-scraper
  "Returns a function that, given a url, counts the number of words in
   its content, among other things"
  [word]
  (let [f (comp (counter word) title url scrape)]
    (fn [url]
      (let [v (f url)]
        (log/info word "=>" (:count v))
        (when (> (:count v) 0)
          (model/add-url (assoc v :term word)))))))

(defn make-scrapers
  "Make scrapers for a sequence of words"
  [& words]
  (if (empty? words)
    (constantly nil)
    (apply juxt (map make-scraper words))))

(defn start
  "Scrape urls looking for words received from the configuration topic"
  [endpoint]
  (let [scraper  (atom (make-scrapers))
        listener (msg/listen endpoint (fn [url] (@scraper url)) :concurrency 10)
        config   (cfg/observe #(reset! scraper (apply make-scrapers %)))]
    (cfg/notify)
    [listener config]))

(defn stop
  "Cleanly shutdown the return value of start"
  [[listener config]]
  (msg/unlisten listener)
  (cfg/ignore config))