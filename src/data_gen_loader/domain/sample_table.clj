(ns data-gen-loader.domain.sample-table)

(def definition {
 :primary-key ["id"]
 :id 12345
 :name "Hello world"
 :town "Earth"
})

(defn create [overrides]
 definition ; TODO
)

(defn create-table []

)


