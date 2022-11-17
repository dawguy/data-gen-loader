(ns data-gen-loader.domain.sample-table
  (:require [next.jdbc.sql :as sql]
            [qbits.alia :as alia]))

(def definition {
 :primary-key [:id :town]
 :id 12345
 :name "Hello world"
 :town "Earth"
})

(defn create-table-schema [auto-key]
 (str "
create table sample_table(
  id     integer " auto-key ",
  name   varchar(255),
  town   varchar(255)
)"))

(defn create-pk-table-schema [auto-key]
 (str "
create table sample_table_inserted_reference(
  pk_sample_table_inserted_reference integer " auto-key ",
  id                                 integer,
  town                               varchar(255)
)"))

(defn get-data
  ([session] (alia/execute (session) "select * from sample_table"))
  ([session id] (alia/execute (session)
                              (alia/prepare (session) "select * from sample_table where id = :id;")
                              {:values {:id (int id)}}))
  )

(defn save-data [session data]
  (let [prepared-insert (alia/prepare (session) "insert into sample_table (id, name, town) values (:id, :name, :town)")]
    (alia/execute (session) prepared-insert {:values {:id (int (:id data))
                                                      :name (:name data)
                                                      :town (:town data)}})
))

(defn save-pk-table-data [db data]
  (sql/insert! (db) :sample_table_inserted_reference data))

(defn delete-data [session id-data]
  (let [prepared-delete (alia/prepare (session) "delete from sample_table where id = :id")]
    (alia/execute (session) prepared-delete {:values {:id (int (:id id-data))}})))

(defn delete-pk-table-data [db id-data]
  (sql/delete! (db) :sample_table_inserted_reference {:id id-data}))

(comment
 (def data {:primary-key ["id"], :id 966, :name "97c3ca35-757c-4fcf-92d3-13f4219e06d3", :town "Earth"})
,)