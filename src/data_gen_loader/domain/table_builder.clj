(ns data-gen-loader.domain.table-builder
  (:require [clojure.string :as string]))

(def table-def-place (with-meta {
                                 :id   "integer"
                                 :name "varchar(255)"
                                 :town "varchar(255)"
                                 }
                                {
                                 :table-name  "tb_place"
                                 :primary-keys {
                                               :id   :auto-increment
                                               :town :primary-key
                                               }
                                 }))

(def table-def-person
  (with-meta {
              :id         "integer"
              :first_name "varchar(255)"
              :last_name  "varchar(255)"
              }
             {
              :table-name  "tb_person"
              :primary-keys {
                            :id :auto-increment
                            }
              }))

(def table-def-child
  (with-meta {:id     "integer"
              :parent "integer"
              :child  "integer"}
             {:table-name  "tb_person_person"
              :primary-keys {
                            :id :auto-increment
                            :parent :primary-key
                            }
              :foreign-keys {
                             :parent #'table-def-person
                             :child  #'table-def-person
                             }
             }))

(comment [table-def] "table-def defines a table. Its expected to be in format"
         (with-meta
            {:field-name "exact wording of field type"
             :other-field-name "integer"}
            {:table-name "name of table"
             :primary-keys {
                           :id :auto-increment
                           :second-pk-if-necessary :primary-key
                           }
             :foreign-keys {:field-name-here #'target-field-def-here}}
            :options [:auto-increment]
           )
         (meta table-def-child)
         (str-auto-key table-def-child "sqlite")
         (str-auto-key table-def-child "mysql")
         (def data table-def-child)
         (def table-def table-def-child)
         (def data {})
         ,)

(defn primary-keyify "Translates primary key definitions into strings" [table-def m]
  (reduce (fn [acc [f primary-type]]
            (-> acc
                (assoc-in [:create-table :primary-keys f] (if (= primary-type :auto-increment)
                                                              (str (symbol f) " " (get table-def f) " primary key autoincrement")
                                                              (str (symbol f) " " (get table-def f) " primary key")))
                (assoc-in [:select :primary-keys f] (symbol f))
                (assoc-in [:value :primary-keys f] (get table-def f))
                ))
          m (:primary-keys (meta table-def))))

(defn regular-keyify "Translates regular table definitions into strings" [table-def m]
  (reduce (fn [acc [f v]]
            (-> acc
                (assoc-in [:create-table :keys f] (str (symbol f) " " v))
                (assoc-in [:select :keys f] (symbol f))
                (assoc-in [:value :keys f] (get table-def f))
                ))
          m table-def))

(defn foreign-keyify "Translates foreign keys into the create table string version" [table-def m]
  (reduce (fn [acc [f foreign-table-def]]
            (-> acc
                (assoc-in [:create-table :foreign-keys f] (str "FOREIGN KEY(" (symbol f) ") REFERENCES "
                                                               (:table-name (meta (deref foreign-table-def)))
                                                               "(" (symbol f) ")"))
                (assoc-in [:select :foreign-keys f] (symbol f))
                (assoc-in [:value :foreign-keys f] (get table-def f))
                ))
          m (:foreign-keys (meta table-def))))

(comment "Helpers for keyify functions"
         (foreign-keyify table-def {})
         (primary-keyify table-def {})
         (regular-keyify table-def {})
         ,)

(declare dissoc-in)

(defn assoc-data-sqlite [table-def] (->> table-def
                                  (primary-keyify table-def)
                                  (regular-keyify table-def)
                                  (foreign-keyify table-def)))

(defn sqlite-create-table-fn [table-def] "Generates a function which takes in a (db) and when executed will create the table defined by the table-def"
  (let [table-name (:table-name (meta table-def))
        create-table-strs (:create-table (assoc-data-sqlite table-def))
        deduped-strs (reduce dissoc-in create-table-strs (map (fn [pk] [:keys pk]) (keys (:primary-keys (meta table-def)))))]
    (str "create table " table-name "("
         (clojure.string/join ", " (flatten (filter identity (conj [] ; Filters out nils
                                       (vals (:primary-keys deduped-strs))
                                       (vals (:keys deduped-strs))
                                       (vals (:foreign-keys deduped-strs))))))
         ");")))

(defn sqlite-select-table-fn [table-def]
  (let [table-name (:table-name (meta table-def))
        select-table-strs (:keys (:select (assoc-data-sqlite table-def)))]
    (fn
      ([] (str "select * from " table-name ";"))
      ([data]
       (let [selected-table-keys (select-keys select-table-strs (keys data))]
         (str "select * from " table-name
              " where " (clojure.string/join ", " (map (fn [[k v]] (str v " = " (get data k))) selected-table-keys))
              ";")))
    )))

(defn sqlite-insert-into-table-fn [table-def]
  (let [table-name (:table-name (meta table-def))
        select-table-strs (get-in (assoc-data-sqlite table-def) [:select :keys])]
    (fn [data]
      (let [selected-table-keys (select-keys select-table-strs (keys data))]
        (str "insert into " table-name
             " (" (clojure.string/join ", " (vals selected-table-keys))
             ") values (" (clojure.string/join ", " (map #(get data %) (keys selected-table-keys)))
             ");")))))

(defn sqlite-update-into-table-fn [table-def]
  (let [table-name (:table-name (meta table-def))
        select-table-strs (get-in (assoc-data-sqlite table-def) [:select])]
    (fn [data]
      (let [selected-table-keys (select-keys (get select-table-strs :keys) (keys data))]
        (str "update " table-name
             " set " (clojure.string/join ", " (map (fn [[k v]] (str v " = " (get data k))) selected-table-keys))
             " where " (clojure.string/join ", " (map (fn [[k v]] (str v " = " (get data k))) (:primary-keys select-table-strs)))
             ";")))))

(defn sqlite-delete-table-fn [table-def]
  (let [table-name (:table-name (meta table-def))
        select-table-strs (get-in (assoc-data-sqlite table-def) [:select])]
    (fn
      ;([] (str "delete from " table-name ";")) ; Optional. Uncomment to allow delete with no where
      ([data]
         (str "delete from " table-name
              " where " (clojure.string/join ", " (map (fn [[k v]] (str v " = " (get data k))) (:primary-keys select-table-strs)))
              ";"))
      )))

(comment ""
         (def table-def table-def-person)
         (def table-def table-def-child)
         (def data {:id 12345 :name "Hello world" :town "Earth"})
         (def data {:id 12345 :name "Hello world" :town "Earth" :first_name "Ozzie" :last_name "Albies"})
         (def db nil)
         (def field-name :id)
         (def auto-increment :auto-increment)
         (def table-name (:table-name (meta table-def)))
         (def foreign-keys (:foreign-keys (meta table-def)))
         (def selected-table-keys (select-keys (:keys select-table-strs) (keys data)))
         ((:table-name (meta (deref (second (first foreign-keys))))))
         (sqlite-create-table-fn table-def)
         ((sqlite-create-table-fn table-def) data db)
         (def strs (:strs (->> table-def
                               (primary-keyify table-def)
                               (regular-keyify table-def)
                               (foreign-keyify table-def)
                               )))
         (def deduped-strs (reduce dissoc-in strs (map (fn [pk] [:keys pk]) (keys (:primary-keys (meta table-def))))))
         (def select-table-strs (:keys (:select (assoc-data-sqlite table-def))))
         (def select-table-strs (get-in (assoc-data-sqlite table-def) [:select]))
         (select-keys select-table-strs (keys data))
         (def select-statement (sqlite-select-table-fn table-def))
         (select-statement data)
         (select-statement)
         (def insert-statement (sqlite-insert-into-table-fn table-def))
         (insert-statement data)
         (def update-statement (sqlite-update-into-table-fn table-def))
         (update-statement data)
         (def delete-statement (sqlite-delete-table-fn table-def))
         (delete-statement data)
         (delete-statement)
         ,)

(defn sqlite-save-fn [table-def]

)

(defn create [create-fn]

)

(defn save [db data save-fn]

)

(defn delete [db data ]

)

; https://stackoverflow.com/questions/14488150/how-to-write-a-dissoc-in-command-for-clojure
(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))