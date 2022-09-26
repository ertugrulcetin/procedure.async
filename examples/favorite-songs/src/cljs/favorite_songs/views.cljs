(ns favorite-songs.views
  (:require
   [favorite-songs.network :as net]
   [reagent.core :as r]
   [favorite-songs.common :as common]))

(defn main-panel []
  (r/create-class
    {:component-did-mount #(net/start-process)
     :reagent-render (fn []
                       [:div
                        [:h1 "Welcome to favorite songs app! "]
                        [common/favorite-songs]])}))
