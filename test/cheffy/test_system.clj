(ns cheffy.test-system
  (:require [clojure.test :refer :all]
            [integrant.repl.state :as state]
            [ring.mock.request :as mock]
            [muuntaja.core :as m]
            [cheffy.auth0 :as auth0]
            [clj-http.client :as http]))

(def token (atom nil))

(defn get-test-token
  [email]
  (->> {:content-type  :json
        :cookie-policy :standard
        :body          (m/encode "application/json"
                                 {:client_id  "CTw5qX7cfrM3pCvKO4BH6FsZ5xEJth1K"
                                  :audience   "https://dev-ovidiu.eu.auth0.com/api/v2/"
                                  :grant_type "password"
                                  :username   email
                                  :password   "Sepulcral94"
                                  :scope      "openid profile email"})}
       (http/post "https://dev-ovidiu.eu.auth0.com/oauth/token")
       (m/decode-response-body)
       :access_token))

(defn create-auth0-user
  [{:keys [connection email password]}]
  (let [auth0 (-> state/system :auth/auth0)]
    (->> {:headers          {"Authorization" (str "Bearer " (auth0/get-management-token auth0))}
          :content-type     :json
          :throw-exceptions false
          :cookie-policy    :standard
          :body             (m/encode "application/json"
                                      {:connection connection
                                       :email      email
                                       :password   password})
          }
         (http/post "https://dev-ovidiu.eu.auth0.com/api/v2/users")
         (m/decode-response-body))))


(defn test-endpoint
  ([method uri]
   (test-endpoint method uri nil))
  ([method uri opts]
   (let [app (-> state/system :cheffy/app)
         request (app (-> (mock/request method uri)
                          (cond-> (:auth opts)
                                  (mock/header :authorization (str "Bearer "
                                                                   (or @token (get-test-token "testing@cheffy.app"))))
                                  (:body opts) (mock/json-body (:body opts)))))]
     (update request :body (partial m/decode "application/json")))))

(comment
  (let [request (test-endpoint :get "/v1/recipes")
        decoded-request (m/decode-response-body request)]
    (assoc request :body decoded-request))
  (test-endpoint :post "/v1/recipes" {:img       "string"
                                      :name      "my name"
                                      :prep-time 30}))