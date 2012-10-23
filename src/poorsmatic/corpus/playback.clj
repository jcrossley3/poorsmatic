(ns poorsmatic.corpus.playback
  (:refer-clojure :exclude (get))
  (:require [clojure.java.io :as io]
            [clj-http.util :as util]))

(def corpus-path "/tmp/corpus")

(defn filter-tweets
  [filter callback]
  (future (doseq [f (sort (.listFiles (io/file corpus-path "tweets")))]
            (Thread/sleep (rand-int 500))
            (callback (read-string (slurp f))))))

(defn close
  [stream]
  (and stream (future-cancel stream)))

(defn get
  [url options]
  (Thread/sleep (+ 500 (rand-int 1000)))
  (read-string (slurp (io/file corpus-path "urls" (util/url-encode url)))))

