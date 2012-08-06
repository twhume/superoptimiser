(ns Drivers.Signum
      (:use [clojure.tools.logging :only (info)]))
(use 'Main.Superoptimise)

; The daddy: superoptimises the signum() function, as per the original Masselin experiments

(let [class-name "SignumTest"
      method-name "signum"
      method-signature "(I)I"
      eq-tests-filter [
                       (fn greater-than-one-even? [i]  (= 1 (invoke-method i method-name 1000)))
                       (fn less-than-minus-one? [i]  (= -1 (invoke-method i method-name -999)))
                       (fn greater-than-one? [i]  (= 1 (invoke-method i method-name 999)))
                       (fn less-than-minus-one-even? [i]  (= -1 (invoke-method i method-name -1000)))
                       (fn one? [i]  (= 1 (invoke-method i method-name 1)))
                       (fn minus-one? [i]  (= -1 (invoke-method i method-name -1)))
                       (fn is-zero? [i]  (= 0 (invoke-method i method-name 0)))
                       ]]
  	(defn -main []
     (time
       (dorun
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