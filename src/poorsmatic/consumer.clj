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

(defn make-observer
  "Return a function that responds to configuration changes"
  [scraper]
  (fn [terms]
    (reset! scraper
            (if (empty? terms)
              (constantly nil)
              (apply juxt (map make-scraper terms))))))

(defn start
  "Scrape urls looking for words received from the configuration topic"
  [endpoint]
  (let [scraper  (atom nil)
        callback (make-observer scraper)]
    (callback (model/get-all-terms))
    [(cfg/observe callback)
     (msg/listen endpoint (fn [url] (@scraper url)) :concurrency 10)]))

(defn stop
  "Cleanly shutdown the return value of start"
  [[config listener]]
  (cfg/ignore config)
  (msg/unlisten listener))