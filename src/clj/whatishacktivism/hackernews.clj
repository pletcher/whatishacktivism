(ns whatishacktivism.hackernews
  (:require [clj-http.client :as client]))

(def version "v0")

(def base-url (str "https://hacker-news.firebaseio.com/" version))

(def opts {:as :json})

(defn req [endpoint]
  (-> (client/get (str base-url "/" endpoint ".json") opts) :body))

(defn best-stories [] (req "beststories"))

(defn new-stories [] (req "newstories"))

(defn top-stories [] (req "topstories"))

(defn story [id] (req (str "item" "/" id)))
