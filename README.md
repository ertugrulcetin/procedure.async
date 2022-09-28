# procedure.async

**procedure.async** provides async procedures for Clojure.

## Installation
[![Clojars Project](https://clojars.org/org.clojars.ertucetin/procedure.async/latest-version.svg)](https://clojars.org/org.clojars.ertucetin/procedure.async)

## reg-pro
**reg-pro** is the core construct for defining async procedures. Let's see how it works with simple examples;

```clj
(reg-pro
  :current-user
  (fn [{:keys [req]}]
    (println "Request: " req)
    {:user (get-user-by-username (-> req :query-params (get "username")))}))
```
What did we do so far?
- Registered procedure with id `:current-user`
- Defined the body
- Fetched username from request, made the DB call to get the user object and returned it.

If we're going to validate procedures, we need to register a validation fn like the below;
```clj
;; This will be called one time.
(register-validation-fn!
  (fn [schema data]
    (or (m/validate schema data)
        (me/humanize (m/explain schema data)))))
```

Let's define the last procedure;
```clj  
(reg-pro
  :get-current-users-favorite-songs
  [:current-user]
  {:data [:map
          [:category string?]]
   :response [:map
              [:songs (vector string?)]]}
  (fn [[current-user {:keys [req data]}]]
    (let [user-id (-> current-user :user :id)
          music-category (:category data)]
      {:songs (get-favorite-songs-by-user-id-and-music-category user-id music-category)})))
```

- We required the `:current-user` procedure as a dependency inside a vector. (**reg-pros can have multiple dependencies - similar to re-frame's reg-sub**)
  - When all dependencies are realized, the procedure's body will be called.
  - Dependencies of a procedure run/realize asynchronously.
- Defined a schema map for both the payload and the response for procedure validation. See the `:data` and `:response` keys.
- Finally, returning the response with a list of songs fetched from the DB.

## Example - [Full example](https://github.com/ertugrulcetin/procedure.async/tree/master/examples/favorite-songs)

Let's prepare our mock data - we assume they are like DB tables.

```clj
(def person-name->person-id {"Michael" 1
                             "Pam" 2
                             "Oscar" 3
                             "Dwight" 4})

(def song-id->title {22 "Tiny Dancer by Elton John"
                     33 "Drop It Like It's Hot by Snoop Dogg"
                     44 "Everybody Hurts by REM"
                     55 "Mambo No. 5 by Lou Bega"
                     66 "Use It by The New Pornographers"
                     77 "Sing by Travis"
                     88 "Ring Around the Rosies - Sung"
                     99 "Here I Go Again by Whitesnake"})

(def person-id->favorite-song-ids
  {1 [22 33]
   2 [44 55]
   3 [66 77]
   4 [88 99]})
```

We need to define our websocket handler - (there is also [an HTTP version](https://github.com/ertugrulcetin/procedure.async/blob/master/examples/favorite-songs/src/clj/favorite_songs/routes/home.clj#L40))
```clj
(require '[procedure.async :refer [dispatch]])

(defn ws-handler [req]
  (-> (http/websocket-connection req)
    (d/chain
      (fn [socket]
        (s/consume
          (fn [payload]
            ;; Dispatching message from the client
            (dispatch (:pro payload) {:data (dissoc payload :pro)
                                      :req req
                                      :socket socket
                                      :send-fn (fn [socket result]
                                                 (s/put! socket result))}))
          socket)))))
```

Here, we're going to define our **reg-pro**s (You can find the [full example here](https://github.com/ertugrulcetin/procedure.async/tree/master/examples/favorite-songs));

```clj
(ns favorite-songs.common
  (:require #?@(:cljs [[re-frame.core :refer [subscribe]]
                       [favorite-songs.network :refer [dispatch-pro]]
                       [favorite-songs.subs]]
                :clj  [[procedure.async :refer [reg-pro register-validation-fn!]]
                       [malli.core :as m]
                       [malli.error :as me]])))

#?(:clj
   (do
     (register-validation-fn!
       (fn [schema data]
         (or (m/validate schema data)
             (me/humanize (m/explain schema data)))))

     (reg-pro
       :get-person-name->person-id-table
       (fn [_]
         (println "Fetching person-name->person-id table...")
         (Thread/sleep 10)
         person-name->person-id))

     (reg-pro
       :get-song-id->title-table
       (fn [_]
         (println "Fetching song-id->title table...")
         (Thread/sleep 30)
         song-id->title))

     (reg-pro
       :get-person-id->favorite-song-ids-table
       (fn [_]
         (println "Fetching person-id->favorite-song-ids table...")
         (Thread/sleep 100)
         person-id->favorite-song-ids))

     (reg-pro
       :get-favorite-songs-by-person
       [:get-person-name->person-id-table 
        :get-song-id->title-table
        :get-person-id->favorite-song-ids-table]
       {:data [:map
               [:data string?]]
        :response [:map
                   [:songs [:vector string?]]]}
       (fn [[person-name->person-id-table
             song-id->title-table
             person-id->favorite-song-ids-table
             {:keys [req socket data]}]]
         (println "Payload is: " data)
         (let [person-name (:data data)
               person-id (get person-name->person-id-table person-name)
               liked-songs-ids (get person-id->favorite-song-ids-table person-id)]
           {:songs (mapv #(get song-id->title-table %) liked-songs-ids)})))))

#?(:cljs
   (defn favorite-songs []
     [:<>
      [:span "Please select a person: "]
      [:select
       {:on-change #(dispatch-pro [:get-favorite-songs-by-person (.-value (.-target %))])}
       [:option {:value "Michael"} "Michael"]
       [:option {:value "Pam"} "Pam"]
       [:option {:value "Oscar"} "Oscar"]
       [:option {:value "Dwight"} "Dwight"]]
      [:br]
      [:span "Favorite songs: " @(subscribe [:favorite-songs])]]))
```
