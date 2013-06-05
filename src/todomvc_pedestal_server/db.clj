(ns todomvc-pedestal-server.db
  (:use [datomic.api :only [db q] :as d]))


(def conn (atom nil))

(defn start [uri]
  (reset! conn (when uri
                 (d/delete-database uri)
                 (d/create-database uri)
                 (d/connect uri)))
  ;; load schema
  (let [schema-tx (read-string (slurp "resources/todomvc-pedestal-server/schema.edn"))]
    @(d/transact @conn schema-tx))
  ;; load data-seed
  (let [seed-tx (read-string (slurp "resources/todomvc-pedestal-server/seed-data.edn"))]
    @(d/transact @conn seed-tx)))
