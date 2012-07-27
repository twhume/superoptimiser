(ns Drivers.Abs)
(use 'Main.Superoptimise)

; Superoptimises a function which returns abs(arg)
;
; Hand-coded version:
; ILOAD_0
; DUP
; IFGT 2
; INEG
; IRETURN


(let [class-name "AbsTest"
      method-name "abs"
      method-signature "(I)I"
      eq-tests-filter [
                       (defn minus-one-to-one? [i]  (= 1 (invoke-method i method-name -1)))
                       (defn large-negative? [i]  (= 987349 (invoke-method i method-name -987349)))
                       (defn large-positive? [i]  (= 123212 (invoke-method i method-name 123212)))
                       (defn one-to-one? [i]  (= 1 (invoke-method i method-name 1)))
                       (defn zero-untouched? [i]  (= 0 (invoke-method i method-name 0)))
                       ]]
  
  

  (time
    (doall
      (superoptimise-pmap 5 class-name method-name method-signature eq-tests-filter))))