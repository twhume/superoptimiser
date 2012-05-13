(ns Scratchpad.core)
(use '[clojure.math.combinatorics])
 
; 91


(def alphabet (map char (concat (range 65 91))))
(def two-letters (repeat 2 alphabet))


(defn no-repeats?
  "returns true if list contains no sequentially repeated items"
  [l]
  (let [tail (first (rest l))] 
    (if (nil? tail) true 
    (if (= (first l) tail) false (recur (rest l))))))

; Write a function which allows us to apply a list of filter functions;
; which ranks these filter functions in descending order of number of failures
; and applies these filter functions in this order

; map of function to number_failures using sorted-map-by
; function which applies this map
; adjust function to update this map
; unit tests

(float ( / (count (filter no-repeats? (apply cartesian-product (repeat 5 alphabet)))) (count (apply cartesian-product (repeat 5 alphabet)))))
  
;(count (apply cartesian-product (repeat 3 alphabet)))
;(map #(apply str %) (apply cartesian-product two-letters))
