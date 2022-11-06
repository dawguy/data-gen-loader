(ns data-gen-loader.gen)

(defn eval-gens[m]
  (reduce (fn [col [k v]]
              (if (fn? v)
                (assoc col k (v))
                col)) m m)
)

; Set of helper functions which can be used to create data at runtime that passes
; a set of criteria. Like spec, but less powerful as it doesn't need to do an validation.
(defn val-gen-count [l]
  (fn [] (count l)))
(defn val-gen-num [min max]
  (fn [] (+ min (rand-int (- max min)))))
(defn val-gen-string []
  (fn [] (random-uuid)))
(defn val-gen-val [val]
  (fn [] val))
(defn val-gen-rand-from-set [s]
  (nth s (rand-int (count s))))

(defn run []
  (prn "We ran!")
)