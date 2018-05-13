(ns whatishacktivism.events
  (:require [whatishacktivism.db :as db]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]]))

;;dispatchers

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :page page)))

(reg-event-db
 :set-hn-story-ids
 (fn [db [_ ids]]
   (assoc db :hn-story-ids ids)))

(reg-event-fx
 :request-hn-story-ids
 (fn [{:keys [db]} _]
   {:http-xhrio {:method :get
                 :uri (str "/stories/top")
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:process-response]
                 :on-failure [:process-error]}
    :db (assoc db :loading? true)}))

(reg-event-fx
 :request-hn-story
 (fn [{:keys [db]} [id]]
   {:http-xhrio {:method :get
                 :uri (str "/stories/top/" id)
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:process-response]
                 :on-failure [:process-error]}
    :db (assoc db :loading? true)}))

(reg-event-db
 :process-response
 (fn [db [_ {:keys [db-key data]}]]
   (-> db
       (assoc :loading? false)
       (assoc db-key (js->clj data)))))

(reg-event-db
 :process-error
 (fn [db [_ error]]
   (-> db
       (assoc :loading? false)
       (assoc :error (js->clj error)))))

(reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))

;;subscriptions

(reg-sub
 :loading?
 (fn [db _]
   (:loading? db)))

(reg-sub
 :hn-story-ids
 (fn [db _]
   (:hn-story-ids db)))

(reg-sub
  :page
  (fn [db _]
    (:page db)))

(reg-sub
  :docs
  (fn [db _]
    (:docs db)))
