(ns org.tomhume.so.PruningTests)

; Lets us measure the effectiveness of the pruning strategies we have in
; place, by running Monte Carlo-style tests; pick a random fertile sequence
; of the length we're testing, see how many children it has with our current
; pruning strategy, and repeat.

(use '[org.tomhume.so.Opcodes])

(defn is-fertile?
  "Filter predicate testing the fertility of the supplied sequence"
  [s]
  (uses-operand-stack-ok? s))

(defn is-valid?
  "Filter predicate testing the validity of the supplied sequence"
  [s]
  true
  (finishes-ireturn? s))

(defn get-children
  "Returns a list of fertile child sequences of the supplied sequence. A fertile sequence may not itself be valid, but has the potential of valid children"
  [s]
  (let [ops (keys opcodes)]
    (filter is-fertile? (map #(conj s %) (keys opcodes)))))

(defn rand-opcode-sequence
  "Returns a single valid opcode sequence, randomly generated, of length n"
  [n]
  (loop [depth 0 s '[]]
    (let [fertile-children (get-children s) ]
    (if (= depth n) s
      (recur (inc depth) (nth fertile-children (rand-int (count fertile-children))))))))

(defn prune-stats
  "Runs a statistical test of the pruning algorithm on sequences from length 1 to n inclusive, return the average number of children available at each length"
  [n]
  (let [num-tests 10000]
	  (for [seq-length (range 0 n)]
	    (float ( / (reduce +
	            (map #(count (get-children %))
	                 (repeatedly num-tests (partial rand-opcode-sequence seq-length)))) num-tests)))))


(defn prune-performance
  "Works out the percentage of the overall search space which a pruned version will need to cover, for sequence length n"
  [n]
  (float ( / (reduce * (prune-stats n))
             (reduce * (repeat n (count (keys opcodes)))))))