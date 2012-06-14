(ns Scratchpad.DepthTree)

(def atoms  '(:a :b :c))

(defn no-double-a?
  [s]
  (= -1 (.indexOf s "aa")))

(defn no-abab?
  [s]
  (= -1 (.indexOf s "abab")))

(defn child-filter? [s] (and (no-double-a? s) (no-abab? s)))

(defn get-children [n] (map #(conj n %) atoms))
;(defn get-children [n] (map #(if (seq? n) (conj n %) (conj (list n) %)) atoms))

; NEARLY works
(defn depth
  [node]
  (if (> (count node) 2) node
    (concat node (map depth (get-children node)))))



(defn depth
  [max-depth]
  (map reverse (rest (tree-seq #(< (count %) max-depth) get-children nil))))

