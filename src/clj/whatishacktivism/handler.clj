(ns whatishacktivism.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [whatishacktivism.layout :refer [error-page]]
            [whatishacktivism.routes.home :refer [home-routes]]
            [whatishacktivism.routes.hackernews :refer [hackernews-routes]]
            [compojure.route :as route]
            [whatishacktivism.env :refer [defaults]]
            [mount.core :as mount]
            [whatishacktivism.middleware :as middleware]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(mount/defstate app
  :start
  (middleware/wrap-base
   (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (-> #'hackernews-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (route/not-found
     (:body {:status 404
             :title "route not found"})))))
