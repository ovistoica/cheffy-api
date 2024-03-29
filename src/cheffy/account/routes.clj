(ns cheffy.account.routes
  (:require [cheffy.middleware :as mw]
            [cheffy.account.handlers :as account]))

(defn routes
  [env]
  (let [db (:jdbc-url env)
        auth0 (:auth0 env)]
    ["/account" {:swagger    {:tags ["account"]}
                 :middleware [[mw/wrap-auth0]]}
     [""
      {:post   {:handler   (account/create-account! db)
                :responses {201 {:body nil?}}
                :summary   "Create an account"}
       :delete {:handler   (account/delete-account! db auth0)
                :responses {204 {:body nil?}}
                :summary   "Delete an account"}
       :put    {:handler   (account/update-role-to-cook! auth0)
                :responses {204 {:body nil?}}
                :summary   "Update user role to cook"}}]]))
