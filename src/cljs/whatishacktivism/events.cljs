(ns whatishacktivism.events
  (:require [whatishacktivism.db :as db]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub subscribe]]))

;;dispatchers

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :page page)))

(reg-event-fx
 :show-stories
 (fn [{:keys [db]} [_ story-idx]]
   {:dispatch-n (list [:set-active-page :story]
                      [:set-active-story-idx (int story-idx)]
                      [:request-story-ids])}))

(reg-event-fx
 :request-story-ids
 (fn [{:keys [db]} _]
   {:http-xhrio {:method :get
                 :uri (str "/stories/top")
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:request-story-ids-success]
                 :on-failure [:process-error]}
    :db (assoc-in db [:loading? :story-ids] true)}))

(reg-event-fx
 :request-story-ids-success
 (fn [{:keys [db]} [_ ids]]
   {:db (-> db
            (assoc-in [:loading? :story-ids] false)
            (assoc :story-ids (js->clj ids)))
    :dispatch [:request-story (nth ids (or (:active-story-idx db) 0))]}))

(reg-event-fx
 :request-story
 (fn [{:keys [db]} [_ id]]
   {:http-xhrio {:method :get
                 :uri (str "/stories/top/" id)
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:request-story-success]
                 :on-failure [:process-error]}
    :db (assoc-in db [:loading? :story] true)}))

(reg-event-fx
 :request-story-success
 (fn [{:keys [db]} [_ raw]]
   (let [story (js->clj raw)
         comment-requests (mapv (fn [id] [:request-comment id]) (:kids story))]
     {:db (-> db
              (assoc-in [:loading? :story] false)
              (assoc :story story))
      :dispatch-n comment-requests})))

(reg-event-fx
 :request-comment
 (fn [{:keys [db]} [_ id]]
   {:http-xhrio {:method :get
                 :uri (str "/stories/top/" id)
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:request-comment-success]
                 :on-failure [:process-error]}
    :db (assoc-in db [:loading? :comments id] true)}))

(reg-event-db
 :request-comment-success
 (fn [db [_ comment]]
   (-> db
       (assoc-in [:loading? :comments (:id comment)] false)
       (assoc-in [:comments (:id comment)] comment))))

(reg-event-db
 :process-error
 (fn [db [_ error]]
   (-> db
       (assoc :loading? false)
       (assoc :error (js->clj error)))))

(reg-event-db
 :set-active-story-idx
 (fn [db [_ idx]]
   (assoc db :active-story-idx idx)))

;;subscriptions

(reg-sub
 :story/loading?
 (fn [db _]
   (get-in db [:loading? :story])))

(reg-sub
 :story-ids/loading?
 (fn [db _]
   (get-in db [:loading? :story-ids])))

(reg-sub
 :comment/loading?
 (fn [db [_ id]]
   (get-in db [:loading? :comments id])))

(reg-sub
 :comment
 (fn [db [_ id]]
   (get-in db [:comments id])))

(reg-sub
 :story-ids
 (fn [db _]
   (:story-ids db)))

(reg-sub
 :active-story-idx
 (fn [db _]
   (:active-story-idx db)))

(reg-sub
 :story
 (fn [db _]
   (:story db)))

(reg-sub
  :page
  (fn [db _]
    (:page db)))
