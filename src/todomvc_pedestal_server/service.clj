(ns todomvc-pedestal-server.service
    (:require [io.pedestal.service.http :as bootstrap]
              [io.pedestal.service.http.route :as route]
              [io.pedestal.service.http.body-params :as body-params]
              [io.pedestal.service.http.route.definition :refer [defroutes]]
              [ring.util.response :as ring-resp]
              [todomvc-pedestal-server.models :as m]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s" (clojure-version))))

(defn home-page
  [request]
  (ring-resp/response "To list todos go to /todos!"))

(defn list-todos [request]
  (let [todos (m/list-todos)]
    (bootstrap/edn-response (str "list-todos" todos))))

(defn create-todo [{:keys [path-params form-params] :as request}]
  (let [ todo {:id  (get form-params "id" "")
               :title (get form-params "title" "") 
               :completed  (Boolean/valueOf (get form-params "completed" "false"))}
        result (m/create-todo todo)]
    (bootstrap/edn-response (str "get-todo" result)))
)


(defn get-todo [{:keys [path-params] :as request}]
  (let [id (:id path-params)
        todo (m/get-todo id)]
    (bootstrap/edn-response todo)))

(defn update-todo [{:keys [path-params form-params] :as request}]
  (let [id (:id path-params)
        todo {:id id
              :title (get form-params "title" "")
              :completed (Boolean/valueOf (get form-params "completed" "false"))}]
    (bootstrap/edn-response (str "update-todo" (m/update-todo todo)))))

(defn delete-todo [{:keys [path-params] :as request}]
  (let [id (:id path-params)
        todo (m/delete-todo id)]
    (bootstrap/edn-response (str "delete-todo" todo))))

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) bootstrap/html-body]
     ["/about" {:get about-page}]
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
