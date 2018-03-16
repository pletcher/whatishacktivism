(ns whatishacktivism.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [whatishacktivism.ajax :refer [load-interceptors!]]
            [whatishacktivism.events])
  (:import goog.History))

(defn nav-link [uri title page]
  [:a.link.dim.f6.f5-ns.dib.mr3
   {:class (if (= page @(rf/subscribe [:page])) "near-black b" "gray")
    :href uri}
   title])

(defn navbar []
  [:nav
   {:role "navigation"}
   [nav-link "#/" "Home" :home]
   [nav-link "#/about" "About" :about]])

(defn card [title body]
  [:article.mw5.mw6-ns.hidden.ba.b--near-white.mv4
   [:h1.f4.mv0.pv2.ph3.bg-near-white.near-black title]
   [:div.pa3
    [:p.f6.f5-ns.lh-copy.measure.mv0 body]]])

(defn about-page []
  (card "About" [:span
                 [:span.i "What is hacktivism?"]
                 " takes as its cue the better known "
                 [:a.i {:href "https://whatisdigitalhumanities.com/"}
                  "What is Digital Humanities?"]
                 ", an app that presents random quotes about digital humanities based on a curated list."]))

(defn home-page []
  [card "Hacktivism is..." "Lorem ipsum dolor sit amet, consetetur sadipscing eliter, sed diam nonumy eirmod..."])


(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  [:div.athelas.near-black.pa3.pa4-ns
   [navbar]
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(rf/dispatch [:set-docs %])}))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
