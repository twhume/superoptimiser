(ns Drivers.GreaterThanZero)
(use 'Main.Superoptimise)

; Superoptimises a function which returns 1 if the argument is greater than zero, 0 otherwise
;
; A hand-coded sequence for this would be
; ILOAD_0
; ICONST_1
; IFGT 2
; ICONST_0
; IRETURN

(let [class-name "GreaterThanZeroTest"
      method-name "gt0"
      method-signature "(I)I"
      eq-tests-filter [
                       (fn odd-larger? [i]  (= 1 (invoke-method i method-name 5)))
                       (fn even-larger? [i]  (= 1 (invoke-method i method-name 8)))
                       (fn large-odd-larger? [i]  (= 1 (invoke-method i method-name 123563)))
                       (fn large-even-larger? [i]  (= 1 (invoke-method i method-name 123212)))
                       (fn handles-zero? [i]  (= 0 (invoke-method i method-name 0)))
                       (fn odd-smaller? [i]  (= 0 (invoke-method i method-name -5)))
                       (fn even-smaller? [i]  (= 0 (invoke-method i method-name -8)))
                       (fn large-odd-smaller? [i]  (= 0 (invoke-method i method-name -98629)))
                       (fn large-even-smaller? [i]  (= 0 (invoke-method i method-name -68222)))
                       ]]
	(defn -main []
	  (time
	    (doall
	      (superoptimise-pmap 5 class-name method-name method-signature eq-tests-filter)))))

