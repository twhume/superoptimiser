(ns Cluster.Client
  (:require [clj-http.client :as client])
  (:use [clojure.tools.logging :only (error)]))

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

