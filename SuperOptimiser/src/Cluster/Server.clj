(ns Cluster.Server
  (:use noir.core)
  (:require [noir.server :as server]))

(def maxnodes 10)
(def currentNode (atom -1))
(def problem "Drivers.Signum")

; Used to start a server which hands out details of the number of nodes, node ID, and Clojure file to run
; Effectively, tells each server which part of the search space to handle

(def server (server/start 8080))

(defpage "/init" []
  (let [next (swap! currentNode inc)]
    (if (>= next maxnodes) "Done"
      (str maxnodes "\n" next "\n" problem))))

