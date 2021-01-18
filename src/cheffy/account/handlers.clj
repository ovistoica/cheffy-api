(ns cheffy.account.handlers
  (:require [ring.util.response :as rr]
            [cheffy.account.db :as account-db]))

(defn create-account!
  [db]
  (fn [request]
    (let [{:keys [sub name picture]} (-> request :claims)]
      (account-db/create-account! db {:uid sub :name name :picture picture})
      (rr/status 204))))

(defn delete-account!
  [db]
  (fn [request]
    (let [uid (-> request :claims :sub)]
      (account-db/delete-account! db {:uid uid}))))