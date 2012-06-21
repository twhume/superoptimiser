(ns Scratchpad.SequenceMatcher)
(use 'clojure.test)
(require '[clojure.zip :as zip])

; add a rule
; add a second rule
; add a third rule, shorter than the second but identical otherwise

; Test rules
; :iload_0 :iload_0
; :iload_0 :pop
; :ineg :ineg
; :swap :swap
; #{storage_op} = (iload_0 iload_1 iload_2 iload_3 :iconst_n :bipush)
; #{neutral_stack_effect} = (ineg)
; #{storage_op} :pop
; #{storage_op} #{neutral-stack-effect} :pop
; #{storage_op} :pop
; #{storage_op} #{storage_op} :pop2
; :dup :pop (does nothing)
; :pop :pop (could be pop2)
; :dup2 :pop :pop
; :dup2 :pop2 
; :iconst_0 :iadd
; :iconst_0 :isub 
; :iconst_1 :idiv ??? (divide by 1)
; :iconst_1 :imul ??? (multiply by 1)
; iconst_0 :imul (could just be push of 0)

(defn new-sm
  "Create a new sequence matcher and return it"
  []
  (zip/seq-zip '()))

(defn node
  "Helper function to quickly make a node from a keyword"
  [k]
  (seq [k]))

(defn add-rule
  "Add a rule r to the sequence matcher sm and return it"
  [sm r]
  (println "Starting add-rule")
    (loop [rule-head r tree-pos (zip/seq-zip sm)]
      (println "Children=" (zip/children tree-pos))
      (println "Node=" (node (first r)))
      
      (if (some #{node (first r)} (zip/children tree-pos))
        (println "MATCH"))
      (if (empty? rule-head) (zip/root tree-pos)
          (recur (rest rule-head) (zip/down (zip/insert-child tree-pos (node (first rule-head)))))
      )))

;(is (= '(:a (:b (:c))) (add-rule nil '(:a :b :c))))

(defn matches? 
  "Does the sequence seq match any of the rules encoded into the sequence matcher sm?"
  [sm seq]
  true)

