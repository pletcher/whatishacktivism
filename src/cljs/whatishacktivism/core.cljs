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

(defn gen-key []
  (gensym "key-"))

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

(defn button [href text]
  [:a.f3.fw6.link.dim.ba.bw2.ph3.pv2.mb2.dib.orange.w-100 {:href href} text])

(defn hn-story [id]
  [:a.link.orange {:href (str "https://news.ycombinator.com/item?id=" id)} "full comments"])

(defn user [id]
  [:a.link.orange {:href (str "https://news.ycombinator.com/user?id=" id)} id])

(defn home-page []
  [:article.vh-100.dt.w-100
   [:div.dtc.v-mid.tc.orange.ph3.ph4-l
    [:h1.f6.f2-m.f-subheadline-l.fw6.tc "Wanna run a quick experiment?"]
    [:span.dib.w-25 (button "#/stories" "Do I have a choice?")]]])

(defn comment-component [id]
  (let [loading? @(rf/subscribe [:comment/loading? id])
        comment @(rf/subscribe [:comment id])
        k (gen-key)]
    (if loading? [:li.ph4.pv3 {:key k} "loading"]
        (if-let [{:keys [by text]} comment]
          [:li.ph4.pv3 {:key k}
           [:span.fw6 (str by ":")]
           [:span.f5.db.lh-copy text]]))))

(defn story-page []
  (let [loading? @(rf/subscribe [:story/loading?])
        story @(rf/subscribe [:story])]
    (if loading?
      [:article.vh-100.dt.w-100
       [:div.dtc.v-mid.tc.orange.ph3.ph4-l
        [:h1.f6.f2-m.f-subheadline-l.fw6.tc "Loading..."]]]
      (let [{:keys [by id kids title url]} story]
        [:article.vh-100.dt.w-100
         [:section.ph3.ph4-l
          [:h1.f-subheadline-l.fw6.mb0.orange.tc title]
          [:h2.tc [:span "submitted by "] (user by)]
          [:h3.tc [:span "source: "] [:a.link.orange {:href url} url]]
          [:h4.tc.mb0 (hn-story id)]]
         [:div.tc.pa4
          [:h2.orange.mb4 "How would you classify the political tone of the discussion below?"]
          [:div.flex.justify-around.ph4
           [:span.w-20 {:on-click #(rf/dispatch [:request-vote-left id])} (button "#/stories" "left-leaning")]
           [:span.w-20 {:on-click #(rf/dispatch [:request-vote-right id])} (button "#/stories" "right-leaning")]]]
         [:div.ph5
          [:ul.list.pl0 (doall (map #(comment-component %) kids))]]]))))

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

(secretary/defroute "/stories" []
  (rf/dispatch [:show-stories]))

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
(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (hook-browser-navigation!)
  (mount-components))
