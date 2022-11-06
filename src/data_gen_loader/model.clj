(ns data-gen-loader.model
  (:require [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql])
  )

;; Database component
(def ^:private my-db
  {:dbtype "sqlite" :dbname "tracking_db"})

(defn- populate
  "Called at application startup. Attempts to create the
  database table and populate it. Takes no action if the
  database table already exists."
  [db db-type]
  (let [auto-key (if (= "sqlite" db-type)
                   "primary key autoincrement"
                   (str "generated always as identity"
                        " (start with 1 increment by 1)"
                        " primary key"))]
    (try
      (jdbc/execute-one! (db)
                         [(str "
create table inserted_row (
  id            integer " auto-key ",
  table_name    varchar(32)
)")])
      (jdbc/execute-one! (db)
                         [(str "
create table inserted_row_key (
  id            integer " auto-key ",
  inserted_row  integer,
  key_name      varchar(32),
  value         varchar(255),
  FOREIGN KEY(inserted_row) REFERENCES inserted_row(id)
)")])
      (jdbc/execute-one! (db)
                         [(str "
create table sample_data_table (
  id            integer " auto-key ",
  first_name    varchar(32),
  last_name     varchar(32)
)")])
      (println "Created database and tables!")
      (catch Exception e
        (println "Exception: " (ex-message e))
        (println "Failed to created database")))))

(defrecord Database [db-spec     ; configuration
                     datasource] ; state

  component/Lifecycle
  (start [this]
    (if datasource
      this ; already initialized
      (let [database (assoc this :datasource (jdbc/get-datasource db-spec))]
        ;; set up database if necessary
        (populate database (:dbtype db-spec))
        database)))
  (stop [this]
    (assoc this :datasource nil))

  ;; allow the Database component to be "called" with no arguments
  ;; to produce the underlying datasource object
  clojure.lang.IFn
  (invoke [_] datasource))

(defn setup-database [] (map->Database {:db-spec my-db}))

(defn get-inserted-data
  "Return all available users, sorted by name.
  inserted_row/id, inserted_row/table_name"
  [db]
  (sql/query (db)
             ["
select a.*, b.*
 from inserted_row a
 join inserted_row_key b ON a.id = b.inserted_row
"]))