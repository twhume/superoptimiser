(ns Drivers.Negate
    (:use [clojure.tools.logging :only (info)]))
(use 'Main.Superoptimise)

; Superoptimises a function which negates its argument
;
; An optimal hand-coded sequence for this would be
; ILOAD_0 INEG IRETURN

(let [class-name "Negate"
      method-name "neg"
      method-signature "(I)I"
      eq-tests-filter [
                       (fn zero-untouched? [i]  (= 0 (invoke-method i method-name 0)))
                       (fn one-to-minus-one? [i]  (= -1 (invoke-method i method-name 1)))
                       (fn minus-one-to-one? [i]  (= 1 (invoke-method i method-name -1)))
                       (fn large-positive? [i]  (= -123212 (invoke-method i method-name 123212)))
                       (fn large-negative? [i]  (= 987349 (invoke-method i method-name -987349)))
                       ]]
  	(defn -main []
     (time
          (doall
            (superoptimise-pmap 3 class-name method-name method-signature eq-tests-filter))))
   
    (defn run-slice
      "Superoptimises a small slice of the overall search space"
      [num-nodes cur-node]
      (do
        (info "starting node " cur-node "/" num-nodes)
	      (time
	          (doall
	            (superoptimise-slice 3 class-name method-name method-signature eq-tests-filter num-nodes cur-node)))
        (info "finishing node " cur-node "/" num-nodes))))