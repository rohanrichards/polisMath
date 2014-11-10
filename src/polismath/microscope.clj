(ns polismath.microscope
  (:require [polismath.conv-man :as cm]
            [polismath.queued-agent :as qa]
            [polismath.db :as db]
            [polismath.conversation :as conv]
            [polismath.named-matrix :as nm]
            [polismath.utils :refer :all]
            [plumbing.core :as pc]
            [korma.core :as ko]
            [korma.db :as kdb]
            [environ.core :as env]
            [clojure.tools.trace :as tr]
            [clojure.tools.logging :as log]
            [clojure.newtools.cli :refer [parse-opts]]))


(defn conv-poll
  [zid]
  (kdb/with-db (db/db-spec)
    (ko/select db/votes
      (ko/where {:zid zid})
      (ko/order [:zid :tid :pid :created] :asc))))


(defn get-zid-from-zinvite
  [zinvite]
  (-> 
    (kdb/with-db (db/db-spec)
      (ko/select "zinvites"
        (ko/fields :zid :zinvite)
        (ko/where {:zinvite zinvite})))
    first
    :zid))


(defn recompute
  [& {:keys [zid zinvite] :as args}]
  (let [_          (assert (xor zid zinvite))
        zid        (or zid (get-zid-from-zinvite zinvite))
        new-votes  (conv-poll zid)
        conv-agent ((cm/new-conv-agent-builder zid))]
    (qa/enqueue conv-agent {:last-timestamp 0 :reactions new-votes})
    (qa/ping conv-agent)
    (add-watch
      (:agent conv-agent)
      :complete-watch
      (fn [k r o n]
        (println "Done recomputing")))
    (:agent conv-agent)))


(defn kw->int
  [kw]
  (-> kw
      (str)
      (clojure.string/replace ":" "")
      (Integer/parseInt)))


(defn load-conv
  [& {:keys [zid zinvite] :as args}]
  (assert (xor zid zinvite))
  (let [zid (or zid (get-zid-from-zinvite zinvite))]
    (->
      (db/load-conv zid)
      (update-in
        [:repness]
        (partial pc/map-keys kw->int)))))


(def cli-options
  [["-z" "--zid ZID" "ZID on which to do a rerun" :parse-fn #(Integer/parseInt %)]
   ["-Z" "--zinvite ZINVITE" "ZINVITE code on which to perform a rerun"]])


(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)
        conv-agent (apply-kwargs recompute options)]
    (add-watch
      conv-agent
      :shutdown-watch
      (fn [k r o n]
        (shutdown-agents)))))

