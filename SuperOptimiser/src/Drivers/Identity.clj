(ns Drivers.Identity)
(use 'Main.Superoptimise)

; Superoptimises the Identity function
;
; ILOAD_0 IRETURN is the optimal answer :)

(let [class-name "IdentityTest"
      method-name "identity"
      method-signature "(I)I"
      eq-tests-filter [
                       (fn one-is-one? [i] (= 1 (invoke-method i method-name 1)))
                       (fn zero-is-zero? [i] (= 0 (invoke-method i method-name 0)))
                       (fn minus-one-is-minus-one? [i] (= -1 (invoke-method i method-name -1)))
                       (fn minint-is-minint? [i] (= Integer/MIN_VALUE (invoke-method i method-name Integer/MIN_VALUE)))
                       (fn maxint-is-maxint? [i] (= Integer/MAX_VALUE (invoke-method i method-name Integer/MAX_VALUE)))
                       (fn one-is-not-zero? [i] (not (= 1 (invoke-method i method-name 0))))
                       (fn one-is-not-minus-one? [i] (not (= 1 (invoke-method i method-name -1))))
                       ]]
	(defn -main []
	  (time
	    (doall
	      (superoptimise-pmap 2 class-name method-name method-signature eq-tests-filter)))))