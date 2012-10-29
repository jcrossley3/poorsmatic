(ns poorsmatic.models
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  (:use [datomic.api :only (q db transact) :as d]
        [immutant.registry :only (fetch)]))

(when-let [uri (:datomic-uri (fetch :project))]
  (d/create-database uri)
  (defonce conn (d/connect uri))
  (transact conn (read-string (slurp (io/resource "schema.dtm")))))

(defn add-term
  [term]
  (d/transact
   conn
   [{:db/id #db/id [:db.part/user]
     :tweet/term (str/lower-case term)}]))

(defn delete-term
  [term]
  (if-let [tid (ffirst (q '[:find ?e :in $ ?term :where
                            [?e :tweet/term ?term]]
                          (db conn)
                          (str/lower-case term)))]
    (transact conn [[:db.fn/retractEntity tid]])))

(defn get-all-terms
  []
  (->> (q '[:find ?term ?t :in $ :where
            [?t :tweet/term ?term]] (db conn))
       (sort-by last)
       (map first)))

(defn add-url
  [{:keys [term url title count]}]
  (let [count-id (d/tempid :db.part/user)
        base-url {:db/id #db/id [:db.part/user]
                  :url/term count-id
                  :url/url url}]
    (transact
     conn
     [{:db/id count-id
       :term-count/term (str/lower-case term)
       :term-count/count count}
      (if-let [url-id (ffirst
                       (q '[:find ?e :in $ ?url :where [?e :url/url ?url]]
                          (db conn) url))]
        {:db/id url-id :url/term count-id}
        (if title
          (assoc base-url :url/title title)
          base-url))])))

(defn find-urls-by-term
  [term]
  (->> (q '[:find ?url ?title ?count
            :in $ ?term
            :where
            [?u :url/term ?t]
            [?t :term-count/term ?term]
            [?u :url/url ?url]
            [?u :url/title ?title]
            [?t :term-count/count ?count]]
          (db conn)
          (str/lower-case term))
       (sort-by #(nth % 2) >)
       (take 10)))
