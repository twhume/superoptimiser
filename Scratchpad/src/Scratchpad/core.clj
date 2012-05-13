(ns Scratchpad.core)
(use '[clojure.math.combinatorics])
 
; 91


(def alphabet (map char (concat (range 65 91))))
(def two-letters (repeat 2 alphabet))


(defn no-repeats?
  "returns true if the sequence supplied contains no consecutively repeated items"
  [l]
  (let [tail (first (rest l))] 
    (if (nil? tail) true 
    (if (= (first l) tail) false (recur (rest l))))))

(defn no-repeats-and-no-As?
  "returns true if the sequence supplied contains no A characters or repeated items"
  [l]
  (and (no-repeats? l) (not (some #(= \A %) l))))

(defn all-true?
  "returns true if the list of expressions supplied all return true"
  [l]
  (every? identity l)
)

;  (filter no-repeats-and-no-As? (apply cartesian-product (repeat 2 alphabet)))
; now genericise the above function to
; 1. take a sequence of tests (start with two)
; 2. use apply to pass l into each test
; 3. use reduce (?) to gather together all results

; Write a function which allows us to apply a list of filter functions;
; which ranks these filter functions in descending order of number of failures
; and applies these filter functions in this order

; map of function to number_failures using sorted-map-by
; function which applies this map
; adjust function to update this map
; unit tests

;(defn multi-filter
;  "Filters against a map of criteria"
  

;(float ( / (count (filter no-repeats? (apply cartesian-product (repeat 5 alphabet)))) (count (apply cartesian-product (repeat 5 alphabet)))))
  
;(count (apply cartesian-product (repeat 3 alphabet)))
;(map #(apply str %) (apply cartesian-product two-letters))
