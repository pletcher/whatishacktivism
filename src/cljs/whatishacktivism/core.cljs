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
  [:a.f3.fw6.link.dim.ba.bw2.ph3.pv2.mb2.dib.bg-orange.white.w-100 {:href href} text])

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
          (if (nil? text) nil
              [:li.ph4.pv3 {:key k}
               [:span.fw6 (str by ":")]
               [:span.f5.db.lh-copy {:dangerouslySetInnerHTML {:__html (md->html text)}}]])))))

(defn description-form []
  (let [description @(rf/subscribe [:description])]
    [:form.mw7
     [:label.clip {:for "description"} "In a word, how would you describe political tone of the discussion below?"]
     [:input#description.bb.bl.bt.b--black-20.black-80.br1-ns.br--left-ns.f6.f5-l.fl.input-reset.lh-solid.pa3.w-100.w-75-m.w-80-l
      {:placeholder "Conservative? Radical? Civil?"
       :type "text"
       :max-length 90
       :name "description"
       :on-change #(rf/dispatch [:set-description (-> % .-target .-value)])
       :value description}]
     [:input.b.bg-orange.bn.br1-ns.br--right-ns.button-reset.f6.f5-l.fl.pointer.pv3.tc.w-100.w-25-m.w-20-l.white
      {:on-click #(do (.preventDefault %)
                      (rf/dispatch [:submit-description]))
       :type "button"
       :value "Submit"}]]))

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
         [:div.cf.pa4.ph5.w-100
          [:h2.orange "In a word, how would you describe the discussion below?"]
          (description-form)]
         [:ul.list (doall (map #(comment-component %) kids))]]))))

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
