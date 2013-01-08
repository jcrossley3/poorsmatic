(ns poorsmatic.test.web
  (:use clojure.test
        poorsmatic.web)
  (:require [clj-http.client :as client]
            [immutant.util :as util]
            [poorsmatic.models :as model]))

(def url (util/app-uri))

(use-fixtures :once
              (fn [f]
                (start)
                (f)
                (stop)))
(use-fixtures :each
              (fn [f]
                (model/clear)
                (f)))

(deftest add-term
  (is (nil? (re-find #"gobbledegook" (:body (client/get url)))))
  (is (= 302 (:status (client/post (str url "/add") {:form-params {:term "gobbledegook"}}))))
  (is (re-find #"gobbledegook" (:body (client/get url))))
  (is (= 1 (count (model/get-terms)))))

(deftest delete-term
  (model/add-term "foo")
  (is (re-find #"form action.*/delete/foo" (:body (client/get url))))
  (is (= 302 (:status (client/post (str url "/delete/foo") {:form-params {:term "foo"}}))))
  (is (empty? (model/get-terms))))


