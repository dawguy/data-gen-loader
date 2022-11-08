(ns data-gen-loader.gen
  (:require [data-gen-loader.domain.sample-table :as sample-table]))

(defn eval-gens[m]
  (reduce (fn [col [k v]]
              (if (fn? v)
                (assoc col k (v))
                col)) m m)
)

(defn create
  ([definition] "Returns the default definition" definition)
  ([definition overrides] "Returns an overridden definition"
   (merge definition overrides)))

(defn to-pk-fields [data definition]
  (reduce (fn [col val] (assoc col val (get data val))) {} (:primary-key definition)))

; Set of helper functions which can be used to create data at runtime that passes
; a set of criteria. Like spec, but less powerful as it doesn't need to do an validation.
(defn val-gen-count [l]
  (fn [] (count l)))
(defn val-gen-num [min max]
  (fn [] (+ min (rand-int (- max min)))))
(defn val-gen-string []
  (fn [] (str (java.util.UUID/randomUUID))))
(defn val-gen-val [val]
  (fn [] val))
(defn val-gen-rand-from-list [s]
  (fn [] (nth s (rand-int (count s)))))

(comment "Samples"
         (val-gen-string)
         (def f (val-gen-rand-from-list ["Earth", "Moon", "Antarctica"]))
         (to-pk-fields
           (eval-gens (#'create sample-table/definition {:name (val-gen-string)
                                 :id (val-gen-num 1 1000)}))
           sample-table/definition)
         (eval-gens (#'create))
         (eval-gens (#'create {}))
         (eval-gens (#'create {:name "WORLD"}))
         (eval-gens (#'create {:name (val-gen-string) :id (val-gen-num 1 1000)}))
         ,)

(defn run []
  (prn "We ran!")
)