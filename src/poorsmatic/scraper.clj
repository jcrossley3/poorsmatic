(ns poorsmatic.scraper
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [clj-http.client :as http]
            [poorsmatic.models :as model]
            [poorsmatic.config :as cfg]
            [immutant.cache :as cache]))

(def terms "The currently active terms"
  (atom nil))

(defn fetch*
  "Returns a hashed response given a url"
  [url]
  (try
    (log/info "Fetching" url)
    (time (http/get url {:socket-timeout 10000 :conn-timeout 10000}))
    (catch Exception e (log/warn (.getMessage e)) {})))
(def fetch-url (cache/memo fetch* "scrape" :idle 10))

(defn attach-counts 
  "Adds counts for each word in @terms under :word-counts"
  [content]
  (println "COUNTING:" (:url content))
  (reduce (fn [{:keys [body] :as content} word]
            (assoc-in content [:word-counts word]
                      (-> (str/lower-case (or body ""))
                          (str/split #"[^\w]+")
                          frequencies
                          (get (str/lower-case word) 0))))
          content @terms))

(defn attach-resolved-url
  "Adds a :url to the response"
  [m]
  (assoc m :url (last (:trace-redirects m))))

(defn attach-title
  "Adds a :title to the response"
  [m]
  (println "TITLEING:" (:url m))
  (assoc m :title (last (re-find #"<title>(.*?)</title>" (:body m "")))))


(defn start
  "Scrape urls looking for words received from the config topic"
  []
  (let [reconfigure (fn [t]
                      (reset! terms t))]
    (reconfigure (model/get-terms))
    (cfg/observe reconfigure)))

(defn stop
  "Cleanly shutdown the return value of start"
  [config]
  (cfg/dispose config))
