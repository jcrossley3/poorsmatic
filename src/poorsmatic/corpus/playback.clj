(ns poorsmatic.corpus.playback
  (:refer-clojure :exclude (get))
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-http.util :as util]
            [clj-time.core :as t]
            [clj-time.format :as tf]))

(def corpus-path "/tmp/corpus")
(def tweet-files (atom (cycle (.listFiles (io/file corpus-path "tweets")))))
(def formatter (tf/formatter "EEE MMM dd HH:mm:ss Z yyyy"))

(defn filter-tweets
  [filter callback]
  (if (not-empty filter)
    (let [terms (str/split filter #",")
          done (atom false)]
      (with-meta
        (future (loop [files @tweet-files
                       n1 (t/now)
                       t1 n1]
                  (let [tweet (read-string (slurp (first files)))
                        t2 (tf/parse formatter (:created_at tweet))]
                    (if (or (t/after? t1 t2) (t/after? (t/now) (t/plus n1 (t/secs (t/in-secs (t/interval t1 t2))))))
                      (do
                        (if (some #(re-find (re-pattern (str "(?i)\\b" % "\\b")) (:text tweet)) terms)
                          (callback tweet))
                        (recur (swap! tweet-files rest) (t/now) t2))
                      (do
                        (Thread/sleep 500)
                        (if-not @done (recur files n1 t1)))))))
        {:done done}))))

(defn close
  [stream]
  (when stream
    (reset! (:done (meta stream)) true)))

(defn get
  [url options]
  (Thread/sleep (+ 500 (rand-int 1000)))
  (read-string (slurp (io/file corpus-path "urls" (util/url-encode url)))))

