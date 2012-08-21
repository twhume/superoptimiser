(ns Filters.StackHeightFilter)
(use 'clojure.test)
(use 'Main.Global)

; The StackHeightFilter checks that the destinations of any branching operation have the same height as each other.
; We get VerifierErrors loading classes where this is not the case

(defn get-stack-heights
  "Return a list of the height of the stack at each instruction in the supplied list"
  [o]
  (butlast (reduce #(conj %1 (+ (last %1) (:opstack-effect (get opcodes (first %2))))) '[0]  o)))

(is (= '(0 1 2 1 0) (get-stack-heights '((:iload_0) (:iload_0) (:ifle 2) (:istore_0) (:ireturn)))))

; Annoyingly, we can use a normal filter to look for stack height at branch destinations... because destinations haven't been filled in yet.

(defn branches-respect-stack-height?
  "Is the stack height the same at instructions following branches and their destinations?"
  [c]
  (if (empty? (:jumps c)) true
    (let [stack-heights (get-stack-heights (:code c))]
      (loop [remainder (keys (:jumps c))]
        (let [src (first remainder) dest (get (:jumps c) src)]
          (if (empty? remainder) true
              (if (not (= (nth stack-heights (inc src)) (nth stack-heights dest))) false
                (recur (rest remainder)))))))))

(is (= false (branches-respect-stack-height? '{:jumps {2 4} :code ((:iload_0) (:iload_0) (:ifle 2) (:istore_0) (:ireturn))})))
(is (= false (branches-respect-stack-height? '{:jumps {2 4} :code ((:iload_0) (:dup) (:if_icmpne 2) (:iconst_m1) (:ireturn))})))
(is (= true (branches-respect-stack-height? '{:jumps {2 5} :code ((:iload_0) (:iload_0) (:ifle 2) (:istore_0) (:iload_0) (:ireturn))})))