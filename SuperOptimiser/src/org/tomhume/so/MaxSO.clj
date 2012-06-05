(ns org.tomhume.so.MaxSO)
(use 'org.tomhume.so.TestMap)
(use 'org.tomhume.so.Main)

(import '(clojure.lang DynamicClassLoader))
(import 'clojure.lang.Reflector)

; Superoptimises the Max() function

; Basic details of the class: class name, method name, method signature

(def class-name "MaxTest")
(def method-name "max")
(def method-signature "(II)I")

; set up a map of equivalence tests

(defn positive-is-bigger? [i]  (= 1 (invoke-method i method-name -1 1)))
(defn positive-is-bigger-revargs? [i]  (= 1 (invoke-method i method-name 1 -1)))
(defn greater-than-zero? [i]  (= 1 (invoke-method i method-name 0 1)))
(defn greater-than-zero-revargs? [i]  (= 1 (invoke-method i method-name 1 0)))
(defn bigger-is-better? [i]  (= 19872 (invoke-method i method-name 12345 19872)))
(defn bigger-is-better-revargs? [i]  (= 20371 (invoke-method i method-name 20371 19872)))
(defn both-negative? [i]  (= -1 (invoke-method i method-name -1 -3)))
(defn both-negative-revargs? [i]  (= -1 (invoke-method i method-name -3 -1)))


(def eq-tests-filter (test-map [positive-is-bigger? positive-is-bigger-revargs? greater-than-zero? greater-than-zero-revargs? bigger-is-better? bigger-is-better-revargs? both-negative? both-negative-revargs?]))

; The code below is a double-check for our tests: basically, does java.lang.Math pass them?
;(let [^DynamicClassLoader cl (new clojure.lang.DynamicClassLoader) math (.loadClass cl "java.lang.Math")]
;  (passes? eq-tests-filter math)
;)
                                   

(time 
  (doall
        (superoptimise 3 class-name method-name method-signature eq-tests-filter)))