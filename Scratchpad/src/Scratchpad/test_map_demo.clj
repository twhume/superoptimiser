(ns Scratchpad.test_map_demo)
(use '[clojure.math.combinatorics])
(use '[Scratchpad.test_map])

; Demonstration of how to use the test_map

; First, set up an alphabet
(def alphabet (map char (concat (range 65 91))))

; Now declare a couple of test functions which operate on sequences of this alphabet
(defn no-repeats?
  "returns true if the sequence supplied contains no consecutively repeated items"
  [l]
  (let [tail (first (rest l))] 
    (if (nil? tail) true 
    (if (= (first l) tail) false (recur (rest l))))))

(defn no-As?
  "Predicate that returns true if the supplied sequence doesn't include A"
  [l]
  (not (some #(= \A %) l))
)

; Declare a test-map which uses these functions
(def tm (test-map [no-As? no-repeats?]))

; Generate a load of candidates from the alphabet, pass them through the test-map
(def start-time (System/currentTimeMillis))
;(println (count (filter #(passes? tm %) (apply cartesian-product (repeat 4 alphabet)))))
(println (count (apply cartesian-product (repeat 4 alphabet))))
(def end-time (System/currentTimeMillis))
(- end-time start-time)

; Dump the test-map and verify that fail counts have grown, and ordering is correct:


;(float ( / (count (filter no-repeats? (apply cartesian-product (repeat 4 alphabet)))) (count (apply cartesian-product (repeat 5 alphabet)))))
  
;(count (apply cartesian-product (repeat 3 alphabet)))
;(map #(apply str %) (apply cartesian-product two-letters))
