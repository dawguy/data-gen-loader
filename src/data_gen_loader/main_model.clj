(ns data-gen-loader.main-model
  (:require [com.stuartsierra.component :as component]
            [qbits.alia :as alia]
            [data-gen-loader.domain.sample-table])
  )

;; Database component
(def ^:private cass-database-spec
  {:session-keyspace "sample_keyspace"
   :contact-points ["172.22.0.2:9042"]
   :load-balancing-local-datacenter "datacenter1"
   :auth-provider-user-name "cassandra"             ; Okay with hard-coding as this is a hobby project. Ideally we'd use environment variables for this or CLI arguments when booting the REPL.
   :auth-provider-password "cassandra"
   :auth-provider-class "PlainTextAuthProvider"})           ; Okay with hard-coding as this is a hobby project. Ideally we'd use environment variables for this or CLI arguments when booting the REPL.

(defrecord Database [db-spec     ; configuration
                     datasource] ; state

  component/Lifecycle
  (start [this]
    (prn (str "Start" this db-spec datasource))
    (if datasource
      this ; already initialized
      (let [database (assoc this :datasource (alia/session cass-database-spec))]
        database)))
  (stop [this]
    (assoc this :datasource nil))

  ;; allow the Database component to be "called" with no arguments
  ;; to produce the underlying datasource object
  clojure.lang.IFn
  (invoke [_] (do (prn datasource) datasource)))

(defn setup-database [] (map->Database cass-database-spec))

(defn get-inserted-data
  ([session] (data-gen-loader.domain.sample-table/get-data session))
  ([session id] (data-gen-loader.domain.sample-table/get-data session id))
  )