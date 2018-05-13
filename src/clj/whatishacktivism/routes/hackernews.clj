(ns whatishacktivism.routes.hackernews
  (:require [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [whatishacktivism.hackernews :as hn]))

(defroutes hackernews-routes
  (GET "/stories/top" []
       (let [ids (hn/top-stories)]
         {:body ids}))
  (GET "/stories/top/:id" [id]
       (let [story (hn/story id)]
         {:body story})))
