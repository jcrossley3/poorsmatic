(ns poorsmatic.test.config
  (:use clojure.test
        poorsmatic.config))

(deftest ^:integration config-notification
  (start)
  (let [p (promise)]
    (try
      (observe (fn [terms] (deliver p terms)))
      (notify :success)
      (is (= :success (deref p 1000 :fail)))
      (finally
       (stop)))))

