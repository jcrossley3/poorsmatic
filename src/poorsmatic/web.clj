(ns poorsmatic.web
  (:use [hiccup core form element middleware]
        [compojure.core :only [GET POST defroutes]]
        [compojure.handler :only [site]]
        [ring.util.response :only [redirect]])
  (:require [poorsmatic.models :as model]
            [poorsmatic.app :as app]
            [immutant.web :as web]
            [immutant.xa :as xa]))

(defn home
  []
  (html
   [:h2 "Poorsmatic"]
   (form-to [:post "/add"]
            (label "term" "Search term: ")
            (text-field "term")
            (submit-button "add"))
   [:ul
    (for [term (model/get-all-terms)]
      [:li
       [:span.term term]
       (form-to [:post (str "/delete/" term)] (submit-button "delete"))
       [:table
        (for [{:keys [url count]} (model/find-urls-by-term term)]
          [:tr [:td count] [:td (link-to url url)]])]])]))

(defn add
  [term]
  (xa/transaction
   (model/add-term term)
   (app/reconfigure)))

(defn delete
  [term]
  (xa/transaction
   (model/delete-term term)
   (app/reconfigure)))

(defroutes routes
  (GET "/" [] (home))
  (POST "/add" [term] (add term) (redirect "."))
  (POST "/delete/:term" [term] (delete term) (redirect "..")))
(def app (-> (site routes) (wrap-base-url)))

(defn start [] (web/start #'app :reload true))
(defn stop [] (web/stop))

