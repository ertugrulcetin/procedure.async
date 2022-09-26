(ns favorite-songs.network
  (:require
   [applied-science.js-interop :as j]
   [re-frame.core :refer [dispatch]]
   [haslett.client :as ws]
   [haslett.format :as format]
   [cljs.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async :as a]
   [msgpack-cljs.core :as msgp])
  (:require-macros
   [cljs.core.async.macros :refer [go-loop]]))

(defonce ws (atom nil))
(defonce sink (atom nil))

(def binary
  (reify format/Format
    (read [_ s] (msgp/unpack s))
    (write [_ v] (msgp/pack v))))

(defn- ws-connect []
  (ws/connect "ws://localhost:3000/ws" {:format binary}))

(defn- connect []
  (go-loop [ws* (ws-connect)]
    (reset! ws ws*)
    (let [stream (a/<! ws*)
          socket (:socket stream)
          on-close (j/get socket :onclose)]
      (if-not (ws/connected? stream)
        (do
          (ws/close stream)
          (a/<! (a/timeout 1000))
          (recur (ws-connect)))
        (do
          (println "Connected!")
          (reset! sink (:sink stream))
          (j/assoc! socket
            :onclose (fn [e]
                       (on-close e)
                       (println "Connection closed!"))
            :onerror (fn [] (.close socket))))))))

(defn dispatch-pro [[pro data]]
  (let [sink @sink]
    (if (and sink (not (closed? sink)))
      (a/put! sink {:pro pro
                    :data data})
      (js/console.error (str "Could not send data to server. " (pr-str data))))))

(defn- process-procedures-responses []
  (go-loop [stream (a/<! @ws)
            source (:source stream)]
    (if (closed? source)
      (println "Closed")
      (let [{:keys [id result error] :as data} (a/<! source)]
        (js/console.log data)
        (if error
          (js/console.error error)
          (dispatch [(keyword (str (name id) "-result")) (pr-str (:songs result))]))
        (recur stream source)))))

(defn start-process  []
  (connect)
  (process-procedures-responses))
