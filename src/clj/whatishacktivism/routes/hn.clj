(ns whatishacktivism.routes.hn
  (:require [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [whatishacktivism.hackernews :as hn]))

(defroutes hn-routes
  (GET "/stories/top" [limit latest]
       (let [stories (hn/top-stories)]
                                        ; take limit after to latest
         ; should probably cache results somewhere...
         )))
