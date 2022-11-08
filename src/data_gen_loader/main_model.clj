(ns data-gen-loader.main-model
  (:require [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [data-gen-loader.domain.sample-table])
  )

;; Database component
(def ^:private pretend-cass-database-spec
  {:dbtype "sqlite" :dbname "cass_db"})

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
                         [(data-gen-loader.domain.sample-table/create-table-schema auto-key)])
      (println "Created main database and tables!")
      (catch Exception e
        (println "Exception: " (ex-message e))
        (println "Looks like main database was already setup")))))

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

(defn setup-database [] (map->Database {:db-spec pretend-cass-database-spec}))