(ns poorsmatic.test.config
  (:use clojure.test
        poorsmatic.config
        [poorsmatic.models :only (add-term delete-term)]))

(deftest ^:integration config-notification
  (start)
  (let [p (promise)]
    (try
      (observe (fn [terms] (deliver p terms)))
      (add-term "success")
      (notify)
      (is (= ["success"] (deref p 1000 :fail)))
      (finally
       (delete-term "success")
       (stop)))))

