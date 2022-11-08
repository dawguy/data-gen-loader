(ns data-gen-loader.domain.sample-table
 (:require [next.jdbc.sql :as sql]))

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

(defn get-data [db]
 (sql/query (db)
            ["
select st.*
  from sample_table st
"]))

(defn save-data [db data]
  (prn (str data))
  (let [id (:id data)]
  (if (and id (not (zero? id)))
   (sql/update! (db) :sample_table
                (dissoc data :id)
                {:id id} {:return-keys [:id :town]})
   (sql/insert! (db) :sample_table
                (dissoc data :id) {:return-keys [:id :town]} ; Note: SQLite does not support return-keys
                ))))

(defn save-pk-table-data [db data]
  (prn (str data))
  (sql/insert! (db) :sample_table_inserted_reference data))

(defn delete-data [db id]
 (sql/delete! (db) :sample_table {:id id}))

(comment
 (def data {:primary-key ["id"], :id 966, :name "97c3ca35-757c-4fcf-92d3-13f4219e06d3", :town "Earth"})
,)