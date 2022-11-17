(ns data-gen-loader.core
  (:require [next.jdbc :refer :all]
            [com.stuartsierra.component :as component]
            [data-gen-loader.main-model :as main-model]
            [data-gen-loader.tracking-model :as tracking-model]
            [data-gen-loader.gen :as gen]
            [com.climate.claypoole :as cp])
  (:import (org.sqlite SQLiteException)))

(defrecord Application [config   ; configuration (unused)
                        database ; dependency
                        state]   ; behavior
  component/Lifecycle
  (start [this]
    (assoc this :state "Running"))
  (stop  [this]
    (assoc this :state "Stopped")))

(defn my-application [options]
  (component/using (map->Application options)
                   [:database]))

(defn new-system ""
  ([port] (new-system port true))
  ([port repl]
   (component/system-map :application (my-application {:repl repl})
                         :database    (main-model/setup-database)
                         :database-key-lookup (tracking-model/setup-database)
                         )))

(comment
  (def system (new-system 8888))
  (:database system)
  (get-in system [:database :datasource])
  (alter-var-root #'system component/start)
  (alter-var-root #'system component/stop)
  (main-model/get-inserted-data (:database system))
  (main-model/get-inserted-data (:database system) 2)
  (main-model/populate (:database system) (:dbtype "sqlite"))
  (data-gen-loader.domain.sample-table/delete-data (:database system) {:id 6065})
  ,)

(defn generate-sample-table-data-future [gen-definition system]
  (try
    (let [data (gen/eval-gens gen-definition)
          gen-id (data-gen-loader.domain.sample-table/save-data (:database system) (apply dissoc data [:primary-key]))]
      (prn "data " data)
      (prn "gen-id " gen-id)
      (data-gen-loader.domain.sample-table/save-pk-table-data (:database-key-lookup system)
                                                              (assoc (select-keys data [:town]) :id gen-id)))
    (catch SQLiteException e
      (println "Exception: " (ex-message e)))))


(defn generate-sample-table-data [qty system]
  (let [pool (cp/threadpool 50)
        overrides {:id   (gen/val-gen-num 0 10000)
                   :name (gen/val-gen-string)
                   :town (gen/val-gen-rand-from-list ["Earth", "Moon", "Antarctica"])}
        gen-definition (gen/create data-gen-loader.domain.sample-table/definition overrides)
        ]
    (doall (cp/pmap pool #(% gen-definition system) (take qty (repeat generate-sample-table-data-future))))
    (cp/shutdown pool)
    nil))

(comment ""
         (time (generate-sample-table-data 1 system))
         (time (generate-sample-table-data 10 system))
         (time (generate-sample-table-data 1000 system))
         (time (generate-sample-table-data 5000 system))
         (time (generate-sample-table-data 10000 system))
         (time (generate-sample-table-data 50000 system))
         ,)