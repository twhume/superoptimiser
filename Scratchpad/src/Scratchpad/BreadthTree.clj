(ns Scratchpad.BreadthTree)

(def atoms '(a b c))

(defn get-children
  [n]
  (map #(str n %) atoms))

(defn add-layer
  ([] (add-layer atoms))
  ([n] 
    (let [children (flatten (map get-children n))]
      (lazy-seq (concat n children (flatten (map get-children children)))))))