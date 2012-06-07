(ns Scratchpad.BreadthTree)

(def atoms '(a b c))

(defn ends-in-c?
  [s]
  (.endsWith s "c"))

(defn no-double-a?
  [s]
  (= -1 (.substring s "aa")))


(defn get-children
  [n]
  (map #(str n %) atoms))

(defn add-layer
  ([] (add-layer atoms))
  ([n] 
    (let [children (flatten (map get-children n))]
      (lazy-seq (concat n children ( add-layer children))))))