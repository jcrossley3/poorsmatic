(ns lobos.config
  (:require [immutant.utilities :as util])
  (:use [lobos [connectivity :only [open-global close-global]]
         [core :only [migrate rollback]]
         [migration :only [*src-directory*]]]))

(def connection-url "file:/tmp/demo;MVCC=TRUE;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE")

(open-global {:classname "org.h2.Driver"
              :subprotocol "h2"
              :subname connection-url
              :unsafe true})

(binding [*src-directory* (util/app-relative "src/")]
  (migrate))

;;; Redeploy won't close the lobos global connection
(close-global)