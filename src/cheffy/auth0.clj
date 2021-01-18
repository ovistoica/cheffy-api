(ns cheffy.auth0
  (:require [clj-http.client :as http]
            [muuntaja.core :as m]))

(defn get-test-token
  []
  (->> {:content-type  :json
        :cookie-policy :standard
        :body          (m/encode "application/json"
                                 {:client_id  "CTw5qX7cfrM3pCvKO4BH6FsZ5xEJth1K"
                                  :audience   "https://dev-ovidiu.eu.auth0.com/api/v2/"
                                  :grant_type "password"
                                  :username   "testing@cheffy.app"
                                  :password   "Sepulcral94"
                                  :scope      "openid profile email"})}
       (http/post "https://dev-ovidiu.eu.auth0.com/oauth/token")
       (m/decode-response-body)
       :access_token))

(comment

  (get-test-token))