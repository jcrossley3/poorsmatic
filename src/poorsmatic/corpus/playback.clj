(ns poorsmatic.corpus.playback
  (:refer-clojure :exclude (get))
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-http.util :as util]))

(def corpus-path "/tmp/corpus")

(defn filter-tweets
  [filter callback]
  (if (not-empty filter)
    (let [terms (str/split filter #",")
          done (atom false)]
      (with-meta
        (future (loop [files (cycle (.listFiles (io/file corpus-path "tweets")))]
                  (let [tweet (read-string (slurp (first files)))]
                    (when (some #(re-find (re-pattern (str "(?i)\\b" % "\\b")) (:text tweet)) terms)
                      (callback tweet)
                      (Thread/sleep (rand-int 1000)))
                    (if-not @done (recur (rest files))))))
        {:done done}))))

(defn close
  [stream]
  (when stream
    (reset! (:done (meta stream)) true)))

(defn get
  [url options]
  (Thread/sleep (+ 500 (rand-int 1000)))
  (read-string (slurp (io/file corpus-path "urls" (util/url-encode url)))))

