(ns data-gen-loader.dev.user
  (:require [data-gen-loader.gen])
)

(defn start [args] 
  (prn "Starting data-gen-loader")
  (data-gen-loader.gen/run)
)

(defn -main []
  (start {}))
