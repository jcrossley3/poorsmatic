(ns poorsmatic.web
  (:use [compojure.core :only [GET POST ANY defroutes]]
        [compojure.route :only [not-found]]
        [compojure.handler :only [site]]
        [hiccup core form element middleware]
        [ring.util.response :only [redirect]])
  (:require [poorsmatic.models :as model]
            [poorsmatic.config :as config]
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
    (for [term (model/get-terms)]
      [:li
       [:b term]
       (form-to [:post (str "/delete/" term)] (submit-button "delete"))
       [:table
        (for [{:keys [url title count]} (model/find-urls-by-term term)]
          [:tr [:td count] [:td (link-to url (or title url))]])]])]))

(defn add
  [term]
  (xa/transaction
   (config/notify (conj (model/get-terms) term))
   (model/add-term term)))

(defn delete
  [term]
  (xa/transaction
   (config/notify (remove #{term} (model/get-terms)))
   (model/delete-term term)))

(defroutes routes
  (GET "/" [] (home))
  (POST "/add" [term]
        (add term)
        (redirect "."))
  (POST "/delete/:term" [term]
        (delete term)
        (redirect ".."))
  (ANY "*" [] (not-found "<h1>Page Not Found</h1>")))

(def app (-> (site routes) (wrap-base-url)))

(defn start [] 
  (config/start)
  (web/start #'app))

(defn stop [] 
  (web/stop)
  (config/stop))
