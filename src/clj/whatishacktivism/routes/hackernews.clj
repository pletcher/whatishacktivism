(ns whatishacktivism.routes.hackernews
  (:require [clojure.java.io :as io]
            [compojure.coercions :refer [as-int]]
            [compojure.core :refer [defroutes GET POST]]
            [hiccup.util :refer [escape-html]]
            [ring.util.http-response :as response]
            [whatishacktivism.db.core :as db]
            [whatishacktivism.hackernews :as hn]))

(defn find-or-create!-story [id url]
  (or (db/find-hn-story {:hn_id id})
      (db/create-hn-story! {:hn_id id :url url})))

(defroutes hackernews-routes
  (GET "/stories/top" []
       (let [ids (hn/top-stories)]
         {:body ids}))
  (GET "/stories/:id" [id]
       (let [story (hn/story id)]
         {:body story}))
  ;; :id refers to the Hacker News Story id,
  ;; not to the story's current index in the
  ;; stories vector
  (POST "/stories/:id/descriptions" {{:keys [id url body]} :params}
        (let [story (find-or-create!-story (Integer/parseInt id) url)
              desc (db/create-description! {:body (escape-html body)
                                            :hn_story_id (:id story)})]
          {:body {:data desc}})))
