(ns todomvc-pedestal-server.service
    (:require [io.pedestal.service.http :as bootstrap]
              [io.pedestal.service.http.route :as route]
              [io.pedestal.service.http.body-params :as body-params]
              [io.pedestal.service.http.route.definition :refer [defroutes]]
              [ring.util.response :as ring-resp]
              [todomvc-pedestal-server.models :as m]))

(defn home-page
  [request]
  (ring-resp/response "To list todos go to http://localhost:8080/todos"))

(defn list-todos [request]
  (if-let [todos (m/list-todos)]
    (bootstrap/edn-response todos)
    (ring-resp/response (str "errors"))))

(defn create-todo [{:keys [path-params form-params] :as request}]
  (let [id (get form-params "id" "") 
        todo {:id  id
              :title (get form-params "title" "") 
              :completed  (Boolean/valueOf (get form-params "completed" "false"))}
        result (m/create-todo todo)]
    (if (m/create-todo todo)
      (bootstrap/edn-response (m/get-todo id))
      (ring-resp/response (str "error")))))


(defn get-todo [{:keys [path-params] :as request}]
  (if-let [todo (m/get-todo (:id path-params))]
    (bootstrap/edn-response todo)
    (ring-resp/response (str "not found"))))

(defn update-todo [{:keys [path-params form-params] :as request}]
  (let [id (:id path-params)
        todo {:id id
              :title (get form-params "title" "")
              :completed (Boolean/valueOf (get form-params "completed" "false"))}]
    (if (m/update-todo todo)
      (bootstrap/edn-response (m/get-todo id))
      (ring-resp/response (str "not found")))))

(defn delete-todo [{:keys [path-params] :as request}]
  (if-let [todo (m/delete-todo (:id path-params))]
    (bootstrap/edn-response "deleted")
    (ring-resp/response (str "error"))))

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) bootstrap/html-body]
     ["/todos" {:get list-todos :post create-todo}]
     ["/todos/:id" {:get get-todo 
                    :put update-todo 
                    :delete delete-todo}]]]])

;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by todomvc-pedestal-server.server/create-server
(def service {;:env :prod
              :env :dev
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::boostrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ;;::bootstrap/host "localhost"
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
