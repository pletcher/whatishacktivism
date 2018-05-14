(ns whatishacktivism.routes.hackernews
  (:require [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [whatishacktivism.hackernews :as hn]))

(defroutes hackernews-routes
  (GET "/stories/top" []
       (let [ids (hn/top-stories)]
         {:body ids}))
  (GET "/stories/:id" [id]
       (let [story (hn/story id)]
         {:body story}))
  (POST "/stories/:id/descriptions" [id params]
        (println params)
        {:body {:ok "ok"}}))
