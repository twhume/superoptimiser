(ns Drivers.SignumSO)
(use 'org.tomhume.so.TestMap)
(use 'Main.Superoptimise)

(import '(clojure.lang DynamicClassLoader))
(import 'clojure.lang.Reflector)

; Superoptimises the signum() function, as per the original Masselin experiments

; Basic details of the class: class name, method name, method signature

(def class-name "SignumTest")
(def method-name "signum")
(def method-signature "(I)I")

; set up a map of equivalence tests

(defn one? [i]  (= 1 (invoke-method i method-name 1)))
(defn minus-one? [i]  (= -1 (invoke-method i method-name -1)))
(defn is-zero? [i]  (= 0 (invoke-method i method-name 0)))
(defn greater-than-one? [i]  (= 1 (invoke-method i method-name 999)))
;(defn greater-than-one-even? [i]  (= 1 (invoke-method i method-name 1000)))
(defn less-than-minus-one? [i]  (= -1 (invoke-method i method-name -999)))
;(defn less-than-minus-one? [i]  (= -1 (invoke-method i method-name -1000)))


(def eq-tests-filter (test-map [one? minus-one? is-zero? greater-than-one? less-than-minus-one?]))

(time (doall (superoptimise-pmap 4 class-name method-name method-signature eq-tests-filter)))