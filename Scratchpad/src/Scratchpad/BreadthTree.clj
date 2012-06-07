(ns Scratchpad.BreadthTree)

(def atoms '("a" "b" "c"))

(defn no-double-a?
  [s]
  (= -1 (.indexOf s "aa")))

(defn no-abab?
  [s]
  (= -1 (.indexOf s "abab")))

(defn child-filter? [s] (and (no-double-a? s) (no-abab? s)))

(defn get-children [n] (map #(str n %) atoms))

(defn add-layer
  ([] (add-layer atoms))
  ([n] 
    (let [child-nodes (filter child-filter? (flatten (map get-children n))) ]
      (lazy-seq (concat n (add-layer child-nodes))))))

; benchmarked at 190 entries/ms on EC2

; GETTING DUPLICATES: (count (distinct (take 20 (add-layer)))) gives 12 ...


; separate out pruning (dropping sections of the tree) from filtering (dropping individual items)


(defn add-layer
  ([] (add-layer atoms))
  ([n] 
    (let [child-nodes (flatten (map get-children n)) ]
      (lazy-seq (concat n (add-layer child-nodes))))))