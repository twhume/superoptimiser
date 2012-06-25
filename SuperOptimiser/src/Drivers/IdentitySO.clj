(ns Drivers.IdentitySO)
(use 'Util.TestMap)
(use 'Main.Superoptimise)

; Superoptimises the Identity function

; Basic details of the class: class name, method name, method signature

(def class-name "IdentityTest")
(def method-name "identity")
(def method-signature "(I)I")

; set up a map of equivalence tests

(defn one-is-one? [i] (= 1 (invoke-method i method-name 1)))
(defn zero-is-zero? [i] (= 0 (invoke-method i method-name 0)))
(defn minus-one-is-minus-one? [i] (= -1 (invoke-method i method-name -1)))
(defn minint-is-minint? [i] (= Integer/MIN_VALUE (invoke-method i method-name Integer/MIN_VALUE)))
(defn maxint-is-maxint? [i] (= Integer/MAX_VALUE (invoke-method i method-name Integer/MAX_VALUE)))
(defn one-is-not-zero? [i] (not (= 1 (invoke-method i method-name 0))))
(defn one-is-not-minus-one? [i] (not (= 1 (invoke-method i method-name -1))))

(def eq-tests-filter (test-map [one-is-one? zero-is-zero? minus-one-is-minus-one? minint-is-minint? maxint-is-maxint? one-is-not-zero? one-is-not-minus-one?]))

(time (doall (superoptimise-pmap 3 class-name method-name method-signature eq-tests-filter)))