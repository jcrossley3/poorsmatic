(ns poorsmatic.models
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [immutant.utilities :as util])
  (:use [datomic.api :only (q db) :as d]))

;; mem uri: "datomic:mem://poorsmatic"
(let [uri "datomic:free://localhost:4334/poorsmatic"]
  (d/create-database uri)
  (defonce conn (d/connect uri)))

(defn tx [body]
  (d/transact conn body))

(defn setup-db []
  (tx (read-string (slurp (io/resource "schema.dtm")))))

(defn q1 [query & args]
  (ffirst (apply q query (db conn) args)))

(defn qe [query & args]
  (d/entity (db conn) (apply q1 query args)))

(defn qes [query & args]
  (let [db (db conn)]
    (->> (apply q query db args)
         (mapv (fn [vals] (mapv (partial d/entity db) vals))))))

(defn add-term  [term]
  (d/transact
   conn
   [{:db/id #db/id [:db.part/user]
     :tweet/term (str/lower-case term)}]))

(defn delete-term
  [term]
  (if-let [t (qe '[:find ?e :in $ ?term :where [?e :tweet/term ?term]]
                 (str/lower-case term))]
    (tx [[:db.fn/retractEntity (:db/id t)]])))

(defn get-all-terms
  []
  (let [db (db conn)]
    (->> (qes '[:find ?e :in $ :where [?e :tweet/term]])
         (map first)
         (sort-by :db/id)
         (map :tweet/term))))

(defn add-url
  [{:keys [term url title count]}]
  (let [count-id (d/tempid :db.part/user)
        base-url {:db/id #db/id [:db.part/user]
                  :url/term count-id
                  :url/url url}]
    (tx [{:db/id count-id
           :term-count/term (str/lower-case term)
           :term-count/count count}
          (if-let [url-id (q1 '[:find ?id :in $ ?url :where [?id :url/url ?url]] url)]
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
