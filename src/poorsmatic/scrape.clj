(ns poorsmatic.scrape
  (:refer-clojure :exclude [count])
  (:require [clj-http.client :as client]
            [clojure.string :as s]
            [clojure.tools.logging :as log]))

(defn scrape [url]
  (try
    (log/info "Fetching" url)
    (time (client/get url {:socket-timeout 10000 :conn-timeout 10000}))
    (catch Exception e {})))

(defn count [word text]
  (-> (s/lower-case (or text ""))
      (s/split #"[^\w]+")
      frequencies
      (get (s/lower-case word) 0)))

(defn word-counter
  "Returns a function that takes a url and returns the number of
   matches for 'word' in its content"
  [word]
  (comp (partial count word) :body scrape))