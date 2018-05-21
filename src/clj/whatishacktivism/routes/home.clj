(ns whatishacktivism.routes.home
  (:require [whatishacktivism.layout :as layout]
            [clojure.string :as string]
            [compojure.core :refer [defroutes GET]]))

(defn home-page []
  (layout/render "home.html"))

(defn stop-words []
  (->
    (slurp "resources/public/stop-words.txt")
    (string/split-lines)))

(defroutes home-routes
  (GET "/" []
       (home-page))
  (GET "/stop-words" []
       {:body (stop-words)}))
