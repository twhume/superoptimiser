(ns Drivers.Signum
      (:use [clojure.tools.logging :only (info)]))
(use 'Main.Superoptimise)
(use 'Main.Bytecode)

; The daddy: superoptimises the signum() function, as per the original Masselin experiments

(let [class-name "SignumTest"
      method-name "signum"
      method-signature "(I)I"
      eq-tests-filter [
                       (fn greater-than-one-even? [i]  (= 1 (invoke-method i method-name 1000)))
                       (fn greater-than-one-even2? [i]  (= 1 (invoke-method i method-name 2094)))
                       (fn less-than-minus-one? [i]  (= -1 (invoke-method i method-name -999)))
                       (fn less-than-minus-one2? [i]  (= -1 (invoke-method i method-name -12458)))
                       (fn greater-than-one? [i]  (= 1 (invoke-method i method-name 999)))
                       (fn greater-than-one2? [i]  (= 1 (invoke-method i method-name 9997)))
                       (fn less-than-minus-one-even? [i]  (= -1 (invoke-method i method-name -1000)))
                       (fn less-than-minus-one-even2? [i]  (= -1 (invoke-method i method-name -12344)))
                       (fn one? [i]  (= 1 (invoke-method i method-name 1)))
                       (fn minus-one? [i]  (= -1 (invoke-method i method-name -1)))
                       (fn is-zero? [i]  (= 0 (invoke-method i method-name 0)))
                       ]]
  	(defn -main []
     (time
       (dorun
        (superoptimise-pmap 7 class-name method-name method-signature eq-tests-filter))))

   (defn equivalent?
     "Is the class-map passes in equivalence to Integer.signum()?"
     [cm]
     (let [class (get-class cm class-name method-name method-signature (:seq-num cm))]
	     (loop [num-tests 100000
	            input 0]
        (if (= 0 num-tests) true
          (if (not (= (Integer/signum input)  (invoke-method class method-name input))) false
            (recur (dec num-tests) (- (quot Integer/MAX_VALUE 2) (rand-int Integer/MAX_VALUE))))))))
 
   (defn check-sequences
     "Takes a list of possible working class-maps, runs a big probabilistic test against each one, and compares to the java.lang.Integer implementation"
     [fns]
     (loop [remainder fns]
       (if (empty? remainder) true
         (do
           (println (equivalent? (first remainder)) (first remainder))
           (recur (rest remainder))))))
   
    (defn run-slice
      "Superoptimises a small slice of the overall search space"
      [num-nodes cur-node]
      (do
        (info "starting node " cur-node "/" num-nodes)
	      (time
	          (dorun
	            (superoptimise-slice 7 class-name method-name method-signature eq-tests-filter num-nodes cur-node)))
        (info "finishing node " cur-node "/" num-nodes))))