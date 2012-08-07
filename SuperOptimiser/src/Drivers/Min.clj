(ns Drivers.Min
        (:use [clojure.tools.logging :only (info)]))
(use 'Main.Superoptimise)

; Superoptimises the Min() function

(let [class-name "MinTest"
      method-name "min"
      method-signature "(II)I"
      eq-tests-filter [
                       (fn negative-is-smaller? [i]  (= -1 (invoke-method i method-name -1 1)))
                       (fn negative-is-smaller-revargs? [i]  (= -1 (invoke-method i method-name 1 -1)))
                       (fn greater-than-zero? [i]  (= 0 (invoke-method i method-name 0 1)))
                       (fn greater-than-zero-revargs? [i]  (= 0 (invoke-method i method-name 1 0)))
                       (fn smaller-is-better? [i]  (= 12345 (invoke-method i method-name 12345 19872)))
                       (fn smaller-is-better-revargs? [i]  (= 12345 (invoke-method i method-name 19872 12345)))
                       (fn both-negative? [i]  (= -3 (invoke-method i method-name -1 -3)))
                       (fn both-negative-revargs? [i]  (= -3 (invoke-method i method-name -3 -1)))
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
