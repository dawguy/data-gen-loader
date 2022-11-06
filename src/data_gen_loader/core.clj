(ns data-gen-loader.core
  (:require [next.jdbc :refer :all]
            [com.stuartsierra.component :as component]
            [data-gen-loader.model :as model])
)

;; Implement your application's lifecycle here:
;; Although the application config is not used in this simple
;; case, it probably would be in the general case -- and the
;; application state here is trivial but could be more complex.
(defrecord Application [config   ; configuration (unused)
                        database ; dependency
                        state]   ; behavior
  component/Lifecycle
  (start [this]
    ;; Component ensures that dependencies are fully initialized and
    ;; started before invoking this component.
    (assoc this :state "Running"))
  (stop  [this]
    (assoc this :state "Stopped")))

(defn my-application [options]
  (component/using (map->Application options)
                   [:database]))

(defn new-system
  "Build a default system to run. In the REPL:

  (def system (new-system 8888))

  (alter-var-root #'system component/start)

  (alter-var-root #'system component/stop)

  See the Rich Comment Form below."
  ([port] (new-system port true))
  ([port repl]
   (component/system-map :application (my-application {:repl repl})
                         :database    (model/setup-database)
                         )))

(comment
  (def system (new-system 8888))
  (alter-var-root #'system component/start)
  (alter-var-root #'system component/stop)
  ;; the comma here just "anchors" the closing paren on this line,
  ;; which makes it easier to put you cursor at the end of the lines
  ;; above when you want to evaluate them into the REPL:
  (model/get-inserted-data (:database system))
  (model/populate (:database system) (:dbtype "sqlite"))
  ,)

