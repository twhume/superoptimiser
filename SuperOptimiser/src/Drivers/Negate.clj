(ns Drivers.Negate)
(use 'Main.Superoptimise)

; Superoptimises a function which negates its argument
;
; An optimal sequence for this would be
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
            (superoptimise-pmap 3 class-name method-name method-signature eq-tests-filter)))))