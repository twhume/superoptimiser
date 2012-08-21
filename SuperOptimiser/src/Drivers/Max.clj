(ns Drivers.Max
        (:use [clojure.tools.logging :only (info)]))
(use 'Main.Superoptimise)

; Superoptimises the Max() function

(let [class-name "MaxTest"
      method-name "max"
      method-signature "(II)I"
      eq-tests-filter [
                       (fn positive-is-bigger? [i]  (= 1 (invoke-method i method-name -1 1)))
                       (fn positive-is-bigger-revargs? [i]  (= 1 (invoke-method i method-name 1 -1)))
                       (fn greater-than-zero? [i]  (= 1 (invoke-method i method-name 0 1)))
                       (fn greater-than-zero-revargs? [i]  (= 1 (invoke-method i method-name 1 0)))
                       (fn bigger-is-better? [i]  (= 19872 (invoke-method i method-name 12345 19872)))
                       (fn bigger-is-better-revargs? [i]  (= 20371 (invoke-method i method-name 20371 19872)))
                       (fn both-negative? [i]  (= -1 (invoke-method i method-name -1 -3)))
                       (fn both-negative-revargs? [i]  (= -1 (invoke-method i method-name -3 -1)))
                       ]]

	(defn -main []
	  (time 
	    (doall
	      (superoptimise-pmap 6 class-name method-name method-signature eq-tests-filter))))

    (defn run-slice
      "Superoptimises a small slice of the overall search space"
      [num-nodes cur-node]
      (do
        (info "starting node " cur-node "/" num-nodes)
	      (time
	          (dorun
	            (superoptimise-slice 6 class-name method-name method-signature eq-tests-filter num-nodes cur-node)))
        (info "finishing node " cur-node "/" num-nodes))))