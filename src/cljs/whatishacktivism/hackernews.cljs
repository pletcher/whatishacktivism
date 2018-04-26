(ns whatishacktivism.hackernews
  (:require [ajax.core :refer [GET]]))

(def version "v0")

(def base-url (str "https://hacker-news.firebaseio.com/" version))

(defn debug-handler [s]
  (println s))

(defn best-stories [f]
  (GET (str base-url "/beststories.json") {:handler f}))

(defn new-stories [f]
  (GET (str base-url "/newstories.json") {:handler f}))

(defn top-stories [f]
  (GET (str base-url "/topstories.json") {:handler f}))

(defn story [id f]
  (GET (str base-url "/item//" id ".json")) {:handler f})
