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

; 1. define a sorted-by-value test-map
; 2. add tests to it
; 3. write a separate no-repeats and no-As functions
; 4. Put these into a test map
; 5. write a filter which uses the test map to filter a stream of combinations
; 6. extend the test map so that it records test failures, and thereby runs failing tests first
; 7. unit tests for all the above


;(float ( / (count (filter no-repeats? (apply cartesian-product (repeat 5 alphabet)))) (count (apply cartesian-product (repeat 5 alphabet)))))
  
;(count (apply cartesian-product (repeat 3 alphabet)))
;(map #(apply str %) (apply cartesian-product two-letters))
