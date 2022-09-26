(ns favorite-songs.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[favorite-songs started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[favorite-songs has shut down successfully]=-"))
   :middleware identity})
