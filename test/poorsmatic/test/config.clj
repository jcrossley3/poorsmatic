(ns poorsmatic.test.config
  (:use clojure.test
        poorsmatic.config))

(deftest ^:integration config-notification
  (start)
  (let [expected (promise)
        observer (observe #(deliver expected %))]
    (try
      (notify :success)
      (is (= :success (deref expected 1000 :fail)))
      (finally
       (dispose observer)
       (stop)))))
