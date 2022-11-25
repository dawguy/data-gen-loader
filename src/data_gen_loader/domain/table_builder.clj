(ns data-gen-loader.domain.table-builder
  (:require [clojure.string :as string]))

(def table-def-place (with-meta {
                                 :id   "integer"
                                 :name "varchar(255)"
                                 :town "varchar(255)"
                                 }
                                {
                                 :table-name  "tb_place"
                                 :primary-key {
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
              :primary-key {
                            :id :auto-increment
                            }
              }))

(def table-def-child
  (with-meta {:id     "integer"
              :parent "integer"
              :child  "integer"}
             {:table-name  "tb_person_person"
              :primary-key {
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
             :primary-key {
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

(defn str-auto-key [db-type]
    (if (= "sqlite" db-type)
      "primary key autoincrement"
      (str "generated always as identity"
           " (start with 1 increment by 1)"
           " primary key")))

(defn sqlite-create-table-fn [table-def] "Generates a function which takes in a (db) and when executed will create the table defined by the table-def"
  (let [table-name (:table-name (meta table-def))
        primary-keys (apply str (map (fn [[field-name auto-increment]]
                                        (if (= auto-increment :auto-increment)
                                          (str (symbol field-name) " " (get table-def field-name) " " (str-auto-key "sqlite") ", ")
                                          (str (symbol field-name) " " (get table-def field-name) " primary key, "))) (:primary-key (meta table-def))))
        non-primary-data (apply dissoc table-def (keys (:primary-key (meta table-def))))
        drop-comma-fn (fn [s following-data] (if (or (< 0 (count following-data)) (empty? s))
                                               s
                                               (.substring s 0 (- (count s) 2))
                                ))
        foreign-keys (:foreign-keys (meta table-def))]
    (fn [data db]
    (apply str "create table " table-name "("
                   primary-keys " "
                   (drop-comma-fn (string/join (apply str (map #(str (symbol (first %)) " " (second %) ", ") non-primary-data)))
                                  (:foreign-keys (meta table-def))) " "
         (drop-comma-fn (apply str (map #(str "FOREIGN KEY(" (symbol (first %)) ") REFERENCES " (:table-name (meta (deref (second %)))) "(" (symbol (first %)) "), ") foreign-keys))
                        [])
         ");")
      ))
  )

(comment ""
         (def table-def table-def-person)
         (def table-def table-def-child)
         (def data {:id 12345 :name "Hello world" :town "Earth"})
         (def db nil)
         (def field-name :id)
         (def auto-increment :auto-increment)
         (def foreign-keys (:foreign-keys (meta table-def)))
         ((:table-name (meta (deref (second (first foreign-keys))))))
         ((sqlite-create-table-fn table-def) data db)
         ,)

(defn sqlite-save-fn [table-def]

)

(defn create [create-fn]

)

(defn save [db data save-fn]

)

(defn delete [db data ]

)

