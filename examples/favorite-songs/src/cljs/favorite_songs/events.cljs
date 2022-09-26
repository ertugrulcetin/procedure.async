(ns favorite-songs.events
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx reg-fx]]
   [favorite-songs.db :as db]))

(reg-event-db
  ::initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :get-favorite-songs-by-person-result
  (fn [db [_ result]]
    (assoc db :favorite-songs result)))
