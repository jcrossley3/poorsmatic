(ns poorsmatic.core
  (:require [poorsmatic
             [web     :as web]
             [twitter :as twitter]
             [scraper :as scraper]
             [models  :as model]]
            [immutant.pipeline :as pl]))

(def application (atom nil))

(defn fan-urls
  "Sends a message on down the pipeline for each url, and halts the
  pipeline for the tweet itself."
  [{:keys [content-urls]}]
  (doseq [url content-urls]
    (when url
      (println "FANNING:" url)
      (pl/*pipeline* url :step pl/*next-step*)))
  pl/halt)

(defn save-urls [data]
  (println "SAVING:" (:url data))
  (doseq [[word count] (:word-counts data)]
    (when (> count 0)
      (model/add-url (assoc data :term word :count count)))))

(defn gen-pipeline []
  (pl/pipeline
   "tweets"
   twitter/attach-urls
   fan-urls
   (pl/step scraper/fetch-url :concurrency 10)
   scraper/attach-resolved-url
   scraper/attach-counts
   scraper/attach-title
   (pl/step save-urls :concurrency 2)
   :error-handler (fn [ex _] (println "ERROR:" ex))))

(defn start
  "Start up everything"
  []
  (when-not @application
    (web/start)
    (let [pipeline (gen-pipeline)]
      (reset! application
              {:pipeline pipeline
               :scraper (scraper/start)
               :daemon (twitter/daemon pipeline)}))))

(defn stop
  "Cleanly shutdown everything "
  []
  (when @application
    (let [{:keys [daemon scraper pipeline]} @application]
      (.stop daemon)
      (scraper/stop scraper)
      (pl/stop pipeline))
    (web/stop)
    (reset! application nil)))
