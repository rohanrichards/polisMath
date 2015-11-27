(ns polismath.system
  (:require [polismath.utils :as utils]
            [polismath.components [db :as db]
                                  [config :as config :refer [create-config]]
                                  [core-matrix-boot :as core-matrix-boot]]
            [polismath.stormspec :as stormspec]
            [com.stuartsierra.component :as component]
            ))

(defn base-system
  "This constructs an instance of the base system components, including config, db, etc."
  [config-overrides]
  (component/system-map
    :config           (create-config config-overrides)
    :core-matrix-boot (component/using (create-core-matrix-booter) [:config])
    :database         (component/using (create-database)           [:config])
    :mongodb          (component/using (create-mongodb)            [:config])
    ))

(defn darwin-system
  "Creates a base-system and assocs in darwin server related components."
  [config-overrides]
  :TODO
  )

(defn storm-system
  "Creates a base-system and assocs in polismath storm worker related components."
  [config-overrides]
  :TODO
  )

(defn onyx-system
  "Creates a base-system and assocs in polismath onyx worker related components."
  [config-overrides]
  :TODO
  )

(defn simulator-system
  "Creates a base-system and assocs in a simulation engine."
  [config-overrides]
  :TODO
  )
