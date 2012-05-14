(ns MessingAbout.test-map )
(use 'clojure.test)

; Test Maps are a concept I use in a couple of places in the SO. They implement a simple
; statistical model: a record is kept of test failures, and tests are run in descending order
; of how often they have previously failed. In this way, I hope to minimise the number of tests
; which must be run.
;
; A Test Map is used in two places:
; 1. To strip out obviously bad bytecode sequences before they are ever converted into a class,
; loaded and run. For instance, any sequence which never returns is obviously bad.
; 2. To order the running of equivalence tests on candidate classes
;

; As we use Test Maps in a couple of places, each one must keep track of its own list of
; test functions. When a Test Map is first initialised, we build a map from the list of 
; functions passed in, and initialise the failure count for each function to 0.

;TODO this map should be a sorted one.
(defn test-map
  "Instantiates a new test-map; pass in a sequence of test functions"
  [flist]
  (atom (into {} (map #(vector % 0) flist))))

;TODO this should increment the fail count for the first failure
(defn passes?
  "Returns true if the sequence provided passes all the tests in the test map provided"
  [tm s]
  (every? true? ((apply juxt (keys @tm)) s)))


; These two trivial functions are used for our unit tests

(defn no-As?
  "Predicate that returns true if the supplied sequence doesn't include A"
  [l]
  (not (some #(= \A %) l))
)

(defn no-Bs?
  "Predicate that returns true if the supplied sequence doesn't include B"
  [l]
  (not (some #(= \B %) l))
)

(def t (test-map [no-As? no-Bs?]))

(is (= true (passes? t '[\E \F \G])))
(is (= true (passes? t '[])))

(is (= false (passes? t '[\A \B \C \D \E])))
(is (= false (passes? t '[\A])))
(is (= false (passes? t '[\B])))
(is (= false (passes? t '[\C \D \A \E])))
(is (= false (passes? t '[\C \D \E \B])))

; Tidy up the namespace after all those tests
(ns-unmap 'MessingAbout.test-map 'no-As?)
(ns-unmap 'MessingAbout.test-map 'no-Bs?)
;(ns-unmap 'MessingAbout.test-map 't)


; This is how you  update the value of a single key in that map. Took me ages to work out:
;(let [key (key (first @t)) ]
; (swap! t conj {key (+ (@t key) 1)})
;  )