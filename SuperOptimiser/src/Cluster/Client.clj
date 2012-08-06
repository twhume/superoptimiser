(ns Cluster.Client
  (:require [clj-http.client :as client])
  (:use [clojure.tools.logging :only (error)]))

; A generic client which connects to a server, is told which problem to attack, and which slice of it, and starts working.
; Usage:
; lein run -m Cluster.Client http://localhost:8080/init

(defn -main [& args]
  (println args)
	(let [endpoint (first args)
	      params (:body (client/get endpoint {:as :clojure}))]
	  (if (empty? params) (error "No parameters received back from server")
	    (do
	      (let [ns-sym (symbol (:problem params))
	            fn-sym (symbol (:problem params) "run-slice")]
	        (use ns-sym)
	        ((eval fn-sym) (:max params) (:node params))))))) 

