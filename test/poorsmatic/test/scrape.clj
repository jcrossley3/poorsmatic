(ns poorsmatic.test.scrape
  (:use clojure.test
        poorsmatic.scrape))

(deftest count-word-matches
  (let [count-foo (counter "foo")
        request {:body "a test for foo, Foo, or FOO, but not food"}]
    (is (= 3 (:count (count-foo request))))))

