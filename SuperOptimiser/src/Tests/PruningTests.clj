(ns Tests.PruningTests)
(use 'Main.Opcodes)
(use 'Main.Global)
(use 'clojure-csv.core)
(use 'clojure.math.combinatorics)

; Lets us measure the effectiveness of the pruning strategies we have in
; place, by running Monte Carlo-style tests; pick a random fertile sequence
; of the length we're testing, see how many children it has with our current
; pruning strategy, and repeat.

(use '[Filters.RedundancyFilter :only (no-redundancy?)])
(use '[Filters.InfluenceFilter :only (retains-influence?)])
(use '[Filters.OperandStackFilter :only (uses-operand-stack-ok?)])
(use '[Filters.VariableUseFilter :only (uses-vars-ok?)])
(use '[Filters.ReturnFilter :only (finishes-ireturn? no-ireturn?)])
(use '[Filters.StackHeightFilter :only (branches-respect-stack-height?)])

(def fertility-filter (partial is-fertile? 1))

(defn gc [n s] (map #(conj s (list %)) (keys opcodes)))

(defn rand-opcode-sequence
  "Returns a single valid opcode sequence, randomly generated, of length n"
  [n]
  (loop [depth 0 s '[]]
    (let [fertile-children (gc 1 s) ]
    (if (= depth n) s
      (recur (inc depth) (nth fertile-children (rand-int (count fertile-children))))))))

(defn prune-stats
  "Runs a statistical test of the pruning algorithm on sequences from length 1 to n inclusive, return the average number of children available at each length"
  [n]
  (let [num-tests 1000]
	  (for [seq-length (range 0 n)]
	    (float ( / (reduce +
	            (map #(count (get-children 1 %))
	                 (repeatedly num-tests (partial rand-opcode-sequence seq-length)))) num-tests)))))


(defn prune-performance
  "Works out the percentage of the overall search space which a pruned version will need to cover, for sequence length n"
  [n]
  (float ( / (reduce * (prune-stats n))
             (reduce * (repeat n (count (keys opcodes)))))))

(defn single-sequence-filter-performance
  "Prints a line of each filter's act on the supplied sequence"
  [filters sequence]
  (map #(if (% sequence) 0 1) filters))

(defn get-combinations ""
  [fields-in data-in]
  (loop [fields fields-in
          data data-in
          entries '()]
     (let [field (first fields)
           item (first data)]
       (if (empty? fields) entries
         (recur (rest fields) (rest data) (if (= 0 item) entries (conj entries (name field))))))))

(defn filter-performance
  "Creates a dump of filter performance"
  [seq-length num-args num-tests]
  (let [filters [(partial retains-influence? num-args)
                 uses-operand-stack-ok?
                 (partial uses-vars-ok? num-args true)
                 finishes-ireturn?
                 (partial no-redundancy? num-args)]
        single-fn (partial single-sequence-filter-performance filters)]
         (map single-fn (repeatedly num-tests (partial rand-opcode-sequence seq-length)))))
 
(defn inc-key
  "Increments the value of the key in the hash, sets it to 1 if it doesn't already exist"
  [h k]
;  (println "inc-key" h k)
  (assoc h k (if (contains? h k) (inc (get h k)) 1)))

(defn update-stats
  "Updates the supplied hash to increment the count for all values in the supplied list"
  [h l]
;  (println "update-stats" h l)
  (loop [hsh h
         remainder l]
    (let [key (first remainder)]
      (if (empty? remainder) hsh
        (recur (inc-key hsh key) (rest remainder))))))

(defn filter-stats
  ""
  [seq-length num-args num-tests]
       (map subsets
            (map (partial get-combinations  '(influence opstack vars return redundancy))
                 (filter-performance seq-length num-args num-tests))))

(defn filter-report
  ""
  [seq-length num-args num-tests]
  (loop [h {}
         remainder (filter-stats seq-length num-args num-tests)]
    (if (empty? remainder) h
      (recur (update-stats h (first remainder)) (rest remainder)))))
