(ns cheffy.middleware
  (:require [ring.middleware.jwt :as jwt]
            [reitit.ring.middleware.exception :as exception]
            [clojure.pprint :as pp]
            [cheffy.recipe.db :as recipe-db]
            [ring.util.response :as rr]
            [next.jdbc.sql :as sql])
  (:import (java.sql SQLException)))
(def wrap-auth0
  {:name        ::auth0
   :description "Middleware for auth0 authentication and authorization"
   :wrap        (fn [handler]
                  (jwt/wrap-jwt handler {:alg          :RS256
                                         :jwk-endpoint "https://dev-ovidiu.eu.auth0.com/.well-known/jwks.json"}))})


;; type hierarchy
(derive ::error ::exception)
(derive ::failure ::exception)
(derive ::horror ::exception)

(defn handler [message exception request]
  (pp/pprint exception)
  {:status 500
   :body   {:message   message
            :exception (.getClass exception)
            :data      (ex-data exception)
            :uri       (:uri request)}})

(def exception-middleware
  (exception/create-exception-middleware
    (merge
      exception/default-handlers
      {;; ex-data with :type ::error
       ::error             (partial handler "error")

       ;; ex-data with ::exception or ::failure
       ::exception         (partial handler "exception")

       ;; SQLException and all it's child classes
       SQLException        (partial handler "sql-exception")

       ;; override the default handler
       ::exception/default (partial handler "default")

       ;; print stack-traces for all exceptions
       ::exception/wrap    (fn [handler e request]
                             (println "ERROR" (pr-str (:uri request)))
                             (handler e request))})))



(def wrap-recipe-owner
  {:name        ::recipe-owner
   :description "Middleware to check if a requestor is a recipe owner"
   :wrap        (fn [handler db]
                  (fn [request]
                    (let [uid (-> request :claims :sub)
                          recipe-id (-> request :parameters :path :recipe-id)
                          recipe (recipe-db/find-recipe-by-id db recipe-id)]
                      (if (= (:recipe/uid recipe) uid)
                        (handler request)
                        (-> (rr/response {:message "You need to be the recipe owner"
                                          :data    (str "recipe-id " recipe-id)
                                          :type    :authorization-required})
                            (rr/status 401))))))})

(def wrap-manage-recipes
  {:name        ::manage-recipes
   :description "Middlware to check if a user can manage recipes"
   :wrap        (fn [handler]
                  (fn [request]
                    (let [roles (get-in request [:claims :https://app.cheffy.com/roles])]
                      (if (some #{"manage-recipes"} roles)
                        (handler request)
                        (-> (rr/response {:message "You need to be a cook to manager recipes"
                                          :data    (:uri request)
                                          :type    :authorization-required})
                            (rr/status 401))))))})

(def wrap-conversation-participant
  {:name        ::conversation-participant?
   :description "Middleware to check if a requestor is an conversation participant"
   :wrap        (fn [handler db]
                  (fn [request]
                    (let [uid (-> request :claims :sub)
                          conversation-id (-> request :parameters :path :conversation-id)
                          conversation (sql/find-by-keys db :conversation {:uid uid :conversation-id conversation-id})]
                      (if (seq conversation)
                        (handler request)
                        (-> (rr/response {:message "You need to be a participant of the conversation to perform this action"
                                          :data    (str "conversation-id " conversation-id)
                                          :type    :authorization-required})
                            (rr/status 401))))))})