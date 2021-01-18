(ns user
  (:require [integrant.repl :as ig-repl]
            [integrant.core :as ig]
            [integrant.repl.state :as state]
            [cheffy.server]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

(ig-repl/set-prep!
  (fn [] (-> "resources/config.edn" slurp ig/read-string)))

(def go ig-repl/go)
(def halt ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(def app (-> state/system :cheffy/app))
(def db (-> state/system :db/postgres))



(comment
  (sql/find-by-keys db :recipe {:public false})
  (sql/delete! db :recipe {:recipe-id "4ac76022-1002-4463-b5f6-462f86835ad0"})
  (sql/update! db :recipe {:name "my-recipe"}
               {:recipe-id "a3dde84c-4a33-45aa-b0f3-4bf9ac997681"})
  (-> (app {:request-method :post
            :uri            "/v1/recipes/"
            :body-params    {:name      "my recipe"
                             :prep-time 49
                             :img       "image-url"}})

      :body
      (slurp))

  (go)
  (halt)
  (reset))