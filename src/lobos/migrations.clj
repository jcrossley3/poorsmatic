(ns lobos.migrations
  (:refer-clojure :exclude [alter drop bigint boolean char double float time])
  (:use (lobos [migration :only [defmigration]] core schema)))

(defmigration add-terms-table
  (up [] (create (table :terms
                        (integer :id :primary-key :auto-inc)
                        (varchar :term 128))))
  (down [] (drop (table :terms))))

(defmigration add-urls-table
  (up [] (create (table :urls
                        (integer :id :primary-key :auto-inc)
                        (varchar :term 128)
                        (varchar :url 1024)
                        (varchar :title 1024)
                        (integer :count))))
  (down [] (drop (table :urls ))))
