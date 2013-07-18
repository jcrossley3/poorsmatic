(ns lobos.config
  (:require [immutant.util :as util]
            [immutant.registry :as registry])
  (:use [immutant.xa :only [datasource wrap]]
        [lobos [connectivity :only [open-global close-global]]
         [core :only [migrate rollback]]
         [migration :only [*src-directory*]]]))

(def db-spec (let [s (:db-spec (registry/get :project))]
               {:datasource (or (wrap s) (datasource "poorsmatic" s))}))

(open-global (merge {:subprotocol "h2"} db-spec))

(binding [*src-directory* (util/app-relative "src/")]
  (migrate))

(close-global)
