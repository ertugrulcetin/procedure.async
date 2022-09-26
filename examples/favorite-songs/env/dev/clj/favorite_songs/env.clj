(ns favorite-songs.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [favorite-songs.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[favorite-songs started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[favorite-songs has shut down successfully]=-"))
   :middleware wrap-dev})
