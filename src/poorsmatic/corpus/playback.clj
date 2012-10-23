(ns poorsmatic.corpus.playback
  (:refer-clojure :exclude (get))
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-http.util :as util]))

(def corpus-path "/tmp/corpus")

(defn filter-tweets
  [filter callback]
  (if (not-empty filter)
    (let [terms (str/split filter #",")]
      (future (doseq [f (cycle (.listFiles (io/file corpus-path "tweets")))]
                (let [s (slurp f)]
                  (if (some #(.contains s %) terms)
                    (callback (read-string s))))
                (Thread/sleep (rand-int 500)))))))

(defn close
  [stream]
  (and stream (future-cancel stream)))

(defn get
  [url options]
  (Thread/sleep (+ 500 (rand-int 1000)))
  (read-string (slurp (io/file corpus-path "urls" (util/url-encode url)))))

