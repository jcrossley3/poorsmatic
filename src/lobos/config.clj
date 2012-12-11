(ns lobos.config
  (:require [immutant.util :as util]
            [immutant.registry :as registry])
  (:use [lobos [connectivity :only [open-global close-global]]
         [core :only [migrate rollback]]
         [migration :only [*src-directory*]]]))

(open-global (:db-spec (registry/get :project)))

(binding [*src-directory* (util/app-relative "src/")]
  (migrate))

(close-global)