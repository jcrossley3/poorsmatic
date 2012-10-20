(ns poorsmatic.scrape
  (:refer-clojure :exclude [count])
  (:require [clj-http.client :as client]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [poorsmatic.models :as model]
            [poorsmatic.config :as cfg]
            [immutant.messaging :as msg]
            [immutant.cache :as cache]))

(defn scrape*
  "Returns a hashed response given a url"
  [url]
  (try
    (log/info "Fetching" url)
    (time (client/get url {:socket-timeout 10000 :conn-timeout 10000}))
    (catch Exception e {})))
(def scrape (cache/memo scrape* "scraped" :idle 10))

(defn counter
  "Adds a :count to the response"
  [word]
  (fn [m]
    (assoc m :count (-> (str/lower-case (:body m ""))
                        (str/split #"[^\w]+")
                        frequencies
                        (get (str/lower-case word) 0)))))

(defn title
  "Adds a :title to the response"
  [m]
  (assoc m :title (last (re-find #"<title>(.*?)</title>" (:body m "")))))

(defn url
  "Adds a :url to the response"
  [m]
  (assoc m :url (last (:trace-redirects m))))

(defn make-scraper
  "Returns a function that, given a url, counts the number of words in its content"
  [word]
  (let [scrape (comp (counter word) title url scrape)]
    (fn [url]
      (let [v (scrape url)
            count (:count v)]
        (log/info word "=>" count (str "\"" (:title v) "\""))
        (when (> count 0)
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