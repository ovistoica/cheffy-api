(ns cheffy.auth0
  (:require [clj-http.client :as http]
            [muuntaja.core :as m]))



(defn get-management-token
  [auth0]
  (->> {:content-type     :json
        :throw-exceptions false
        :cookie-policy    :standard
        :body             (m/encode "application/json"
                                    {
                                     :client_id     "vW4kmxPGnPsfscsLglIN5K75OfaIY3RM"
                                     :client_secret (:client-secret auth0)
                                     :audience      "https://dev-ovidiu.eu.auth0.com/api/v2/"
                                     :grant_type    "client_credentials"
                                     })}
       (http/post "https://dev-ovidiu.eu.auth0.com/oauth/token")
       (m/decode-response-body)
       :access_token))

(defn get-manage-recipe-role
  [token]
  (->> {:headers          {"Authorization" (str "Bearer " token)}
        :content-type     :json
        :throw-exceptions false
        :cookie-policy    :standard
        :body             (m/encode "application/json"
                                    {
                                     :client_id     "vW4kmxPGnPsfscsLglIN5K75OfaIY3RM"
                                     :client_secret "xhlpY_BX4MUGjCCjXcEJVaJuiukh_w6gtYbOi6nowUVcxm16ZzwwHtmoao_VZuwh"
                                     :audience      "https://dev-ovidiu.eu.auth0.com/api/v2/"
                                     :grant_type    "client_credentials"
                                     })}
       (http/get "https://dev-ovidiu.eu.auth0.com/api/v2/roles")
       (m/decode-response-body)
       (filter (fn [role] (= (:name role) "manage-recipes")))
       (first)
       :id
       ))




(comment
  (get-management-token)
  (get-manage-recipe-role)
  (get-test-token)
  (create-auth0-user {:connection "Username-Password-Authentication"
                      :email      "account-testing@cheffy.app"
                      :password   "Sepulcral94"}))