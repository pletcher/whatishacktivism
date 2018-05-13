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

;; TODO: Allow user to sort HN item sources based on political leanings; store and learn to predict?

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

(defn start-button []
  [:a.f3.fw6.link.dim.ba.bw2.ph3.pv2.mb2.dib.orange
   {:href "#/stories/1"}
   "Do I have a choice?"])

(defn home-page []
  [:article.vh-100.dt.w-100
   [:div.dtc.v-mid.tc.orange.ph3.ph4-l
    [:h1.f6.f2-m.f-subheadline-l.fw6.tc "Wanna run a quick experiment?"]
    (start-button)]])

(defn loading []
  [:article.vh-100.dt.w-100
   [:div.dtc.v-mid.tc.orange.ph3.ph4-l
    [:h1.f6.f2-m.f-subheadline-l.fw6.tc "Loading..."]]])

(defn story-page []
  (let [loading? @(rf/subscribe [:loading?])
        story @(rf/subscribe [:story])]
    (if loading? (loading) [:h1 (str (:title story) " by " (:by story) " from " (:url story))])))

(def pages
  {:about #'about-page
   :home #'home-page
   :story #'story-page})

(defn page []
  [:main.avenir-next.near-black
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

(secretary/defroute "/stories/:idx" [idx]
  (rf/dispatch [:show-stories idx]))

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
