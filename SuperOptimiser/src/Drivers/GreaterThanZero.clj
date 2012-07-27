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

; Basic details of the class: class name, method name, method signature

(def class-name "GreaterThanZeroTest")
(def method-name "identity")
(def method-signature "(I)I")

; set up a map of equivalence tests

(defn odd-larger? [i]  (= 1 (invoke-method i method-name 5)))
(defn even-larger? [i]  (= 1 (invoke-method i method-name 8)))
(defn large-odd-larger? [i]  (= 1 (invoke-method i method-name 123563)))
(defn large-even-larger? [i]  (= 1 (invoke-method i method-name 123212)))
(defn handles-zero? [i]  (= 0 (invoke-method i method-name 0)))
(defn odd-smaller? [i]  (= 1 (invoke-method i method-name -5)))
(defn even-smaller? [i]  (= 1 (invoke-method i method-name -8)))
(defn large-odd-smaller? [i]  (= 1 (invoke-method i method-name -98629)))
(defn large-even-smaller? [i]  (= 1 (invoke-method i method-name -68222)))


(def eq-tests-filter [odd-larger? handles-zero? even-smaller? even-larger? odd-smaller? large-odd-larger? large-even-larger? large-odd-smaller? large-even-smaller?])

(time 
  (doall
    (superoptimise-pmap 5 class-name method-name method-signature eq-tests-filter)))