(ns poorsmatic.test.config
  (:use clojure.test
        poorsmatic.config))

(deftest ^:integration config-notification
  (start)
  (let [p (promise)
        o (observe (fn [terms] (deliver p terms)))]
    (try
      (notify :success)
      (is (= :success (deref p 1000 :fail)))
      (finally
       (ignore o)
       (stop)))))
