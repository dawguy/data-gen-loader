(ns data-gen-loader.gen
  (:require [data-gen-loader.domain.sample-table :as st])
)

(defn run [] 
  (prn "We ran!")
  (st/create)
)
