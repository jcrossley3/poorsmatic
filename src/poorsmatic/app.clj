(ns poorsmatic.app
  (:use [poorsmatic.handlers :only [make-multiword-scraper make-url-extractor]])
  (:require [clojure.tools.logging :as log]
            [immutant.messaging :as msg]
            [poorsmatic tweets models]))

(def tweets-endpoint "/queue/tweets")
(def urls-endpoint   "/queue/urls")

(def
  ^{:doc "The running state of the application"
    :private true}
  application (atom nil))

(defn ^:private replace-listener
  [m k v]
  (if-let [old (get-in m [:listeners k])]
    (msg/unlisten old))
  (assoc-in m [:listeners k] v))

(defn reconfigure
  "Triggers daemon to reconfigure with new search terms"
  []
  (let [terms (poorsmatic.models/get-all-terms)]
    (poorsmatic.tweets/configure terms)
    (let [scraper (msg/listen urls-endpoint (make-multiword-scraper terms))]
      (swap! application replace-listener :scraper scraper))))

(defn start
  "Start up the application's resources"
  []
  (when-not @application
    (swap! application assoc :daemon
           (poorsmatic.tweets/daemon #(msg/publish tweets-endpoint %)))
    (swap! application replace-listener :url-extractor
           (msg/listen tweets-endpoint (make-url-extractor urls-endpoint)))
    (reconfigure)))

(defn stop
  "Cleanly shutdown the application's resources"
  []
  (.stop (:daemon @application))
  (doseq [listener (vals (:listeners @application))] (msg/unlisten listener))
  (reset! application nil))
