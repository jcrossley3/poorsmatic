(ns poorsmatic.models
  (:require lobos.config
            [clojure.string :as str])
  (:use [korma db core]
        [immutant.xa :only [datasource]]))

(defonce ds (datasource "demo" {:adapter "h2" :database "file:/tmp/demo"}))
(defdb prod {:datasource ds})

(defentity urls)
(defentity terms)

(defn add-term
  [term]
  (let [t (str/lower-case term)]
    (if (empty? (select terms (where (= :term t))))
      (insert terms (values {:term t})))))

(defn delete-term
  [term]
  (delete terms (where (= :term (str/lower-case term)))))

(defn get-all-terms
  []
  (map :term (select terms)))

(defn add-url
  [attrs]
  (insert urls (values attrs)))

(defn find-urls-by-term
  [term]
  (select urls
          (where (= :term (str/lower-case term)))
          (limit 10)
          (order :count :DESC)))
