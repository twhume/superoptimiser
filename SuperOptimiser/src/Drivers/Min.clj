(ns Drivers.Min)
(use 'Main.Superoptimise)

; Superoptimises the Min() function

(let [class-name "MinTest"
      method-name "min"
      method-signature "(II)I"
      eq-tests-filter [
                       (fn positive-is-bigger? [i]  (= -1 (invoke-method i method-name -1 1)))
                       (fn positive-is-bigger-revargs? [i]  (= -1 (invoke-method i method-name 1 -1)))
                       (fn greater-than-zero? [i]  (= 0 (invoke-method i method-name 0 1)))
                       (fn greater-than-zero-revargs? [i]  (= 0 (invoke-method i method-name 1 0)))
                       (fn bigger-is-better? [i]  (= 12345 (invoke-method i method-name 12345 19872)))
                       (fn bigger-is-better-revargs? [i]  (= 19872 (invoke-method i method-name 20371 19872)))
                       (fn both-negative? [i]  (= -3 (invoke-method i method-name -1 -3)))
                       (fn both-negative-revargs? [i]  (= -3 (invoke-method i method-name -3 -1)))
                       ]]

	(defn -main []
	  (time 
	    (doall
	      (superoptimise-pmap 6 class-name method-name method-signature eq-tests-filter)))))
