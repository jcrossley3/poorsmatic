(ns poorsmatic.models
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [immutant.utilities :as util])
  (:use [datomic.api :only (q db) :as d]))

;; mem uri: "datomic:mem://poorsmatic"
(let [uri "datomic:free://localhost:4334/poorsmatic"]
  (d/create-database uri)
  (defonce conn (d/connect uri)))

(defn setup-db []
  (d/transact conn (read-string (slurp (io/resource "schema.dtm")))))

(defn q1 [query db & args]
  (let [r (apply q query db args)]
    (d/entity db (ffirst r))))

(defn qall [query db & args]
  (->> (apply q query db args)
       (mapv #((partial d/entity db) (first %)))))

(defn add-term  [term]
  (d/transact
   conn
   [{:db/id #db/id [:db.part/user]
     :tweet/term (str/lower-case term)}]))

(defn delete-term
  [term]
  (if-let [t (q1 '[:find ?e :in $ ?term :where [?e :tweet/term ?term]]
                 (db conn)
                 (str/lower-case term))]
    (d/transact conn [[:db.fn/retractEntity (:db/id t)]])))

(defn get-all-terms
  []
  (let [db (db conn)]
    (->> (qall '[:find ?e :in $ :where [?e :tweet/term]] db)
         (sort-by :db/id)
         (map :tweet/term))))

(defn add-url
  [{:keys [term url title count]}]
  (d/transact
   conn
   [{:db/id #db/id [:db.part/user]
     :url/term (str/lower-case term)
     :url/url url
     :url/title (or title "[no title]")
     :url/count count}]))

(defn find-urls-by-term
  [term]
  (let [db (db conn)]
    (->> (qall '[:find ?e :in $ ?term :where [?e :url/term ?term]]
               db (str/lower-case term))
         (sort-by :url/count >)              
         (take 10)
         (mapv #(into {} (mapv (fn [[k v]] [(keyword (name k)) v]) %))))))
