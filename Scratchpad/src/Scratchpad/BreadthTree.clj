(ns Scratchpad.BreadthTree)

(def atoms '(a b c))

(defn no-double-a?
  [s]
  (= -1 (.indexOf s "aa")))

(defn no-abab?
  [s]
  (= -1 (.indexOf s "abab")))

(defn child-filter? [s] (and (no-double-a? s) (no-abab? s)))

;(defn get-children [n]  (map #(str n %) atoms))
(defn get-children [n] (map #(if (seq? n) (conj n %) (conj (list n) %)) atoms))

(defn add-layer-filtering
  ([] (add-layer-filtering atoms))
  ([n] 
    (let [child-nodes (filter child-filter? (mapcat get-children n)) ]
      (lazy-seq (concat n (add-layer-filtering child-nodes))))))

; benchmarked at 190 entries/ms on EC2

; GETTING DUPLICATES: (count (distinct (take 20 (add-layer)))) gives 12 ...


; separate out pruning (dropping sections of the tree) from filtering (dropping individual items)



(defn add-layer
  ([] (add-layer atoms))
  ([n] 
    (let [child-nodes (mapcat get-children n) ]
      (lazy-seq (concat n (add-layer child-nodes))))))

(defn add-layer-assisted
  ([] (add-layer-assisted atoms))
  ([n] (concat n (lazy-seq (add-layer-assisted (mapcat get-children n))))))

(defn add-children [n] (println n) (lazy-seq (mapcat get-children n)))

(defn get-ch [n] (map #(str n %) atoms))

(defn add-ch 
  ([] (apply concat (iterate add-ch atoms)))
  ([n] (mapcat get-ch n)))

;(last (take 20000000 (add-ch)))

(def fib (lazy-cat [0 1] (map + fib (rest fib))))

(defn child-of
  ; return the seq concatted with the child of entry n
  [s n]
  (concat s (get-ch (nth s n))))

(defn tree 
  [init max-size]
  (loop [n 0 s init]
    (if (= max-size (.length (last s))) s
      (recur (inc n) (child-of s n)))))
