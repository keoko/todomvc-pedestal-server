(ns todomvc-pedestal-server.models
  (:use [datomic.api :only [db q transact] :as d]
        [todomvc-pedestal-server.db :only [conn]]))

(defn get-todo-ref [id]
  (ffirst (q '[:find ?t
               :in $ ?id
               :where [?t :todo/id ?id]]
             (db @conn) id)))


(defn create-todo [{:keys [id title completed]}]
  @(transact @conn [{ :db/id (d/tempid :db.part/db)
                     :todo/id id
                     :todo/title title
                     :todo/completed completed}]))

(defn get-todo-map [todo]
  {:id (:todo/id todo)
   :title (:todo/title todo)
   :completed (:todo/completed todo)})


(defmulti get-todo
  "get todo map by todo identifier, datomic id ref, etc."
  (fn [id] (class id)))

;; todo: use multimethods by class (string then 1, else 2)
(defmethod get-todo java.lang.String
  [id]
  (when-let [todo (d/entity (db @conn) (get-todo-ref id))]
    (get-todo-map todo)))


(defmethod get-todo :default
  [ref]
  (when-let [todo (d/entity (db @conn) ref)]
    (get-todo-map todo)))


(defn update-todo [{:keys [id title completed] :as todo}]
  (when-let [todo-id (get-todo-ref id)]
    @(d/transact @conn [{:db/id todo-id 
                         :todo/title title
                         :todo/completed completed}])))


(defn delete-todo [id]
  (when-let [todo-id (get-todo-ref id)]
    @(d/transact @conn [[:db.fn/retractEntity todo-id]])))


(defn list-todos []
  (let [todos (q '[:find ?t
                     :where [?t :todo/id]]
                   (db @conn))]
    (map #(get-todo-by-ref (first %)) todos)))
