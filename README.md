# procedure.async

A Clojure library designed to ... well, that part is up to you.

## Example

Let's prepare our mock data - they are like DB tables.

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

Here, we're going to define our **reg-pro**s;

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
         (println "Fetching person-name->person-id table...")
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

## License

Copyright © 2022 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
