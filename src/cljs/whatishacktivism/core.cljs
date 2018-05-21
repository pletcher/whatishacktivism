(ns whatishacktivism.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [ajax.core :refer [GET POST]]
            [cljsjs.showdown]
            [whatishacktivism.ajax :refer [load-interceptors!]]
            [whatishacktivism.events])
  (:import goog.History))

(defn gen-key []
  (gensym "key-"))

(let [conv-class (.-Converter js/showdown)
      converter (conv-class.)]
  (defn md->html [s]
    (.makeHtml converter s)))

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

(defn button [href text]
  [:a.f3.fw6.link.dim.ba.bw2.ph3.pv2.mb2.dib.bg-orange.white.w-100 {:href href} text])

(defn hn-story [id]
  [:a.link.orange {:href (str "https://news.ycombinator.com/item?id=" id)} "full comments"])

(defn user [id]
  [:a.link.orange {:href (str "https://news.ycombinator.com/user?id=" id)} id])

(defn home-page []
  [:article.vh-100.dt.w-100
   [:div.dtc.v-mid.tc.orange.ph3.ph4-l
    [:h1.f6.f2-m.f-subheadline-l.fw6.tc "Wanna run a quick experiment?"]
    [:span.dib.w-25 (button "#/stories" "Sure!")]]])

(defn comment-component [id]
  (let [loading? @(rf/subscribe [:comment/loading? id])
        comment @(rf/subscribe [:comment id])
        k (gen-key)]
    (if loading? [:li.ph4.pv3 {:key k} "loading"]
        (if-let [{:keys [by text]} comment]
          (if (nil? text) nil
              [:li.ph4.pv3 {:key k}
               [:span.fw6 (str by ":")]
               [:span.f5.db.lh-copy {:dangerouslySetInnerHTML {:__html (md->html text)}}]])))))

(defn description-form []
  (let [s @(rf/subscribe [:description-input])
        on-click #(do (.preventDefault %)
                      (rf/dispatch [:submit-description]))]
    [:div.mw7
     [:label.clip {:for "description"} "In a word, how would you describe political tone of the discussion below?"]
     [:input#description.bb.bl.bt.b--black-20.black-80.br1-ns.br--left-ns.f6.f5-l.fl.input-reset.lh-solid.pa3.w-100.w-75-m.w-80-l
      {:placeholder "Conservative? Radical? Civil?"
       :type "text"
       :max-length 90
       :name "description"
       :on-change #(rf/dispatch [:set-description-input (-> % .-target .-value)])
       :on-key-down #(when (= (.-which %) 13)
                       (on-click %))
       :value s}]
     [:a.bg-orange.bn.br1-ns.br--right-ns.btn.f5.fl.fw6.pointer.pv3.tc.w-100.w-25-m.w-20-l.white
      {:on-click on-click} "Submit"]]))

(defn show-descriptions-link []
  (if @(rf/subscribe [:descriptions-shown?])
    [:p.black-80
     "It looks like there aren't enough descriptions to show anything useful yet. "
     [:a.orange.pointer {:on-click #(rf/dispatch [:hide-descriptions])} "(hide)"]]
    [:a.f3.fw6.orange.pointer
     {:on-click #(rf/dispatch [:show-descriptions])}
     "Show descriptions"]))

(defn story-page []
  (let [loading? @(rf/subscribe [:story/loading?])
        story @(rf/subscribe [:story])]
    (if loading?
      [:article.vh-100.dt.w-100
       [:div.dtc.v-mid.tc.orange.ph3.ph4-l
        [:h1.f6.f2-m.f-subheadline-l.fw6.tc "Loading..."]]]
      (let [{:keys [by id kids title url]} story]
        [:article
         [:section.ph3.ph4-l
          [:h1.f-subheadline-l.fw6.mb0.orange.tc title]
          [:h2.tc [:span "submitted by "] (user by)]
          [:h3.tc [:span "source: "] [:a.link.orange {:href url} url]]
          [:h4.tc.mb0 (hn-story id)]]
         [:div.pa4.ph5.w-100
          [:h2.orange "In a word, how would you describe the discussion below?"]
          [:div.cf (description-form)]
          [:div.cf.pt4 (show-descriptions-link)]]
         [:ul.list (doall (map #(comment-component %) kids))]]))))

(def pages
  {:home #'home-page
   :story #'story-page})

(defn page []
  [:main.avenir-next.near-black
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

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
