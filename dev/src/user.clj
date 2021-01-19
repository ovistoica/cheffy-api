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
  (reset)
  (let [conversations (sql/find-by-keys db :conversation {:uid "auth0|5ef440986e8fbb001355fd9c"})]
    (for [{:conversation/keys [conversation-id]} conversations
          :let [created-at (sql/query db ["SELECT created_at FROM message
                                            WHERE conversation_id = ?
                                            ORDER BY created_at DESC
                                            LIMIT 1" conversation-id])]]))


  (sql/find-by-keys db :conversation {:uid "auth0|5ef440986e8fbb001355fd9c"})
  (jdbc/execute-one! db ["SELECT created_at FROM message
                                              WHERE conversation_id = ?
                                              ORDER BY created_at DESC
                                              LIMIT 1" "8d4ab926-d5cc-483d-9af0-19627ed468eb"])

  (jdbc/execute-one! db ["SELECT uid FROM conversation
                                                   WHERE uid != ? AND conversation_id = ?"
                         "auth0|5ef440986e8fbb001355fd9c" "8d4ab926-d5cc-483d-9af0-19627ed468eb"])


  (with-open [conn (jdbc/get-connection db)]
    (let [conn-options (jdbc/with-options conn (:options db))
          conversations (sql/find-by-keys conn :conversation {:uid "auth0|5ef440986e8fbb001355fd9c"})]
      (doall
        (for [{:conversation/keys [conversation-id] :as conversation} conversations
              :let [{:message/keys [created-at]}
                    (jdbc/execute-one! conn-options ["SELECT created_at FROM message
                                              WHERE conversation_id = ?
                                              ORDER BY created_at DESC
                                              LIMIT 1" conversation-id])
                    with (jdbc/execute-one! conn-options ["SELECT uid FROM conversation
                                                   WHERE uid != ? AND conversation_id = ?" "auth0|5ef440986e8fbb001355fd9c" conversation-id])
                    [{:account/keys [name picture]}] (sql/find-by-keys conn-options :account with)]]
          (assoc conversation :conversation/updated-at created-at
                              :with-name name
                              :with-picture picture)))))

  )