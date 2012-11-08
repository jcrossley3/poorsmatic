(ns poorsmatic.scraper
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [clj-http.client :as http]
            [poorsmatic.models :as model]
            [poorsmatic.config :as cfg]
            [immutant.messaging :as msg]
            [immutant.cache :as cache]))

(defn fetch*
  "Returns a hashed response given a url"
  [url]
  (try
    (log/info "Fetching" url)
    (time (http/get url {:socket-timeout 10000 :conn-timeout 10000}))
    (catch Exception e (log/warn (.getMessage e)) {})))
(def fetch (cache/memo fetch* "scrape" :idle 10))

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

(defn save-url-for
  "Returns a function that, given a url, counts the number of words in
   its content, and saves the url if count > 0"
  [word]
  (let [scrape (comp (counter word) title url fetch)]
    (fn [url]
      (let [v (scrape url)]
        (log/info word "=>" (:count v))
        (when (> (:count v) 0)
          (model/add-url (assoc v :term word)))))))

(defn start
  "Scrape urls looking for words received from the config topic"
  [endpoint]
  (let [scraper  (atom nil)
        reconfigure (fn [terms]
                      (reset! scraper
                              (if (empty? terms)
                                (constantly nil)
                                (apply juxt (map save-url-for terms)))))]
    (reconfigure (model/get-terms))
    [(cfg/observe reconfigure)
     (msg/listen endpoint (fn [url] (@scraper url)) :concurrency 10)]))

(defn stop
  "Cleanly shutdown the return value of start"
  [[config listener]]
  (cfg/dispose config)
  (msg/unlisten listener))
