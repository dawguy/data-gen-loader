(ns data-gen-loader.core
  (:require [next.jdbc :refer :all]
            [com.stuartsierra.component :as component]
            [data-gen-loader.main-model :as main-model]
            [data-gen-loader.tracking-model :as tracking-model]
            [data-gen-loader.gen :as gen])
)

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
  (alter-var-root #'system component/start)
  (alter-var-root #'system component/stop)
  (main-model/get-inserted-data (:database system))
  (main-model/populate (:database system) (:dbtype "sqlite"))
  ,)


(defn generate-sample-table-data [qty system]
  (let [overrides {:name (gen/val-gen-string)
                   :town (gen/val-gen-rand-from-list ["Earth", "Moon", "Antarctica"])}
        gen-definition (gen/create data-gen-loader.domain.sample-table/definition overrides)
        ]
    ; TODO: Goal is to do this next part in parallel. No idea how to do that right now though, so will be doing it in sequence.
    ; https://www.youtube.com/watch?v=BzKjIk0vgzE -- claypoole talk
    ; Alternative strategy will be to use core.aysnc channels. Maybe do both to learn more?
    (doseq [i (range qty)]
      (let [data (gen/eval-gens gen-definition)
            gen-id (second (first (data-gen-loader.domain.sample-table/save-data (:database system) (dissoc (dissoc data :id) :primary-key))))]
          (data-gen-loader.domain.sample-table/save-pk-table-data (:database-key-lookup system)
            (assoc (select-keys data [:town]) :id gen-id)))
          )))

(comment ""
         (generate-sample-table-data 10 system)
         ,)