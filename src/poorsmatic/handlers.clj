(ns poorsmatic.handlers
  (:require [clojure.tools.logging :as log]
            [immutant.messaging :as msg]))

(defn make-url-extractor
  "Returns a function that parses a tweet for a URL and, if found,
   publishes it to the passed endpoint"
  [endpoint]
  (fn [{tweet :text}]
    (when-let [url (and tweet (re-find #"http:[^\s]*" tweet))]
      (log/info tweet)
      (msg/publish endpoint url))))
