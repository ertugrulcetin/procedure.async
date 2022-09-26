(ns favorite-songs.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :favorite-songs
 (fn [db]
   (:favorite-songs db)))
