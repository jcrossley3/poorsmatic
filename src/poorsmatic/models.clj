(ns poorsmatic.models
  (:require lobos.config
            [clojure.string :as str]
            [immutant.util :as util])
  (:use [korma db core]
        [immutant.xa :only [datasource]]))

(when (util/in-immutant?)
  (defonce ds (datasource "demo" {:adapter "h2" :database lobos.config/connection-url}))
  (defdb prod {:datasource ds}))

(defentity urls)
(defentity terms)

(defn add-term [term]
  (let [t (str/lower-case term)]
    (if (empty? (select terms (where (= :term t))))
      (insert terms (values {:term t})))))

(defn delete-term [term]
  (delete terms (where (= :term (str/lower-case term)))))

(defn get-terms []
  (map :term (select terms)))

(defn add-url [attrs]
  (if (empty? (select urls (where (and (= :url (:url attrs))
                                       (= :term (:term attrs))))))
    (insert urls (values (select-keys attrs [:term :url :title :count])))))

(defn find-urls-by-term [term]
  (select urls
          (where (= :term (str/lower-case term)))
          (limit 10)
          (order :count :DESC)))
