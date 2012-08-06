(ns Cluster.Client
  (:require [clj-http.client :as client])
  (:use [clojure.tools.logging :only (error)]))

(let [endpoint "http://localhost:8080/init"
      params (:body (client/get endpoint {:as :clojure}))]
  (if (empty? params) (error "No parameters received back from server")
    (do
      (let [ns-sym (symbol (:problem params))
            fn-sym (symbol (:problem params) "run-slice")]
        (use ns-sym)
        ((eval fn-sym) (:max params) (:node params))))))
    


; connect to endpoint
; read the max-nodes, cur-node, problem
; launch the problem with the associated parameters

;(def maxnodes 10)
;(def currentNode (atom -1))
;(def problem "Drivers.Signum")
