(ns Cluster.Server
  (:use noir.core)
  (:require [noir.server :as server]))

(def server (server/start 8080))
(def currentNode (atom -1))

; Used to start a server which hands out details of the number of nodes, node ID, and Clojure file to run
; Effectively, tells each server which part of the search space to handle

; Usage:
; lein run -m Cluster.Server 3 Drivers.Negate
; (first argument: number of nodes; second argument: problem to solve)

(defn -main [& args]
  
  (defpage "/init" []
  (let [next (swap! currentNode inc)
        maxnodes (Integer/parseInt (first args))
        problem (second args)]
    (if (>= next maxnodes) "{}"
      (str (hash-map :node next :max maxnodes :problem problem))))))