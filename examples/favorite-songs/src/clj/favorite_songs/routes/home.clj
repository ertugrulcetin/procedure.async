(ns favorite-songs.routes.home
  (:require
   [clojure.java.io :as io]
   [favorite-songs.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :refer [content-type ok]]
   [aleph.http :as http]
   [manifold.deferred :as d]
   [manifold.stream :as s]
   [procedure.async :as pro.async]
   [msgpack.core :as msg]
   [msgpack.clojure-extensions]
   [favorite-songs.common]))

(defn home-page [request]
  (content-type
    (ok
      (-> "public/index.html" io/resource slurp))
    "text/html; charset=utf-8"))

(defn ws-handler [req]
  (-> (http/websocket-connection req)
    (d/chain
      (fn [socket]
        (s/consume
          (fn [payload]
            (let [payload (msg/unpack payload)]
              (pro.async/dispatch (:pro payload) {:data payload
                                                  :req req
                                                  :socket socket
                                                  :send-fn (fn [socket result]
                                                             (s/put! socket (msg/pack result)))})))
          socket)))
    (d/catch
     (fn [_]
       {:status 400
        :headers {"content-type" "application/text"}
        :body "Expected a websocket request."}))))

(defn http-handler [{{:keys [pro data]} :params :as req}]
  {:status 200
   :body (pro.async/dispatch-sync pro {:req req
                                       :data data})})

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/ws" {:get ws-handler}]
   ["/http" {:post http-handler}]])
