(ns Filters.RedundancyFilter)
(use 'clojure.test)
(use 'org.tomhume.so.Opcodes)

; The redundancy filter looks for sequences of redundant operations in a list of JVM opcodes. It does this
; by creating a data structure corresponding to the state of frame; by versioning writes to the frame;
; and by looking to see whether the overall state of a frame is unchanged between any two sets of operations.
; If a sequence of operations has not changed the state of a frame, that sequence is by definition redundant.
; If a candidate sequence of opcodes contains any redundant operations, it is by definition not optimal.


(defn init-state
  "Initialises a map for the state of the code sequence, with the number of arguments passed in"
  [num-args]
  {:stack '()    ; A structure corresponding to the operand stack of the sequence
   :max-var 0    ; The highest version number we've given to a variable
   :max-const 0  ; The highest version number we've given to a constant
   :max-calc 0   ; The highest version number we've given to a calculation
   :vars (apply assoc {} (interleave (range num-args) (repeat 0)))})

(defn inc-max
  "Increments a max value in the state map passed in"
  [which state-map]
  (assoc state-map which (inc (which state-map))))

; When we add a state 

(defn add-state
  "Updates the state map with a single extra opcode"
  [state-map opcode]
  (let [stack (:stack state-map)]
	  (case opcode
	    :bipush (inc-max :max-const (assoc state-map :stack (conj stack [:constant (:max-const state-map)])))
      :dup (assoc state-map :stack (conj stack (first stack)))
      
;      :dup_x1
;      :dup_x2
;      :dup2
;      :dup2_x1
;      :dup2_x2
      
      :pop (assoc state-map :stack (rest stack))
      :pop2 (assoc state-map :stack (nthrest stack 2))
      :swap (assoc state-map :stack (concat (list (second stack) (first stack)) (nthrest stack 2)))
      
      (or
        :iadd
        :iand
        :idiv
        :imul
        :ior
        :irem
        :isub
        :ixor) (inc-max :max-calc (assoc state-map :stack (cons [:calc (:max-calc state-map)] (nthrest stack 2))))
      
      
      :iconst_m1 (assoc state-map :stack (cons [:value -1] stack))
      :iconst_0 (assoc state-map :stack (cons [:value 0] stack))
      :iconst_1 (assoc state-map :stack (cons [:value 1] stack))
      :iconst_2 (assoc state-map :stack (cons [:value 2] stack))
      :iconst_3 (assoc state-map :stack (cons [:value 3] stack))
      :iconst_4 (assoc state-map :stack (cons [:value 4] stack))
      :iconst_5 (assoc state-map :stack (cons [:value 5] stack))

              :iinc
              
              :iload_0
              :iload_1
              :iload_2
              :iload_3
              :ineg
              :ireturn
              :ishl
              :ishr
              
              :istore_0
              :istore_1
              :istore_2
              :istore_3
              :iushr
	    )))

; Lots of unit tests...

(is (=
      '{:stack ([:constant 0]), :max-var 0, :max-const 1, :max-calc 0, :vars {0 0}}
      (add-state (init-state 1) :bipush)))

(is (= '{:stack ([:constant 0] [:constant 0]), :max-var 0, :max-const 1, :max-calc 0, :vars {0 0}}
       (add-state (add-state (init-state 1) :bipush) :dup)))

(is (=
      '{:stack ([:constant 0]), :max-var 0, :max-const 1, :max-calc 0, :vars {0 0}}
      (add-state
        '{:stack ([:constant 1] [:constant 0]), :max-var 0, :max-const 1, :max-calc 0, :vars {0 0}}
        :pop)))

(is (=
      '{:stack ([:constant 0]), :max-var 0, :max-const 1, :max-calc 0, :vars {0 0}}
      (add-state
        '{:stack ([:constant 2] [:constant 1] [:constant 0]), :max-var 0, :max-const 1, :max-calc 0, :vars {0 0}}
        :pop2)))

(is (=
      '{:stack ([:constant 1] [:constant 2] [:constant 3]), :max-var 0, :max-const 4, :max-calc 0, :vars {0 0}}
      (add-state
        '{:stack ([:constant 2] [:constant 1] [:constant 3]), :max-var 0, :max-const 4, :max-calc 0, :vars {0 0}}
        :swap)))

(is (= '{:stack ([:calc 0] [:constant 3]), :max-var 0, :max-const 4, :max-calc 1, :vars {0 0}}
      (add-state
        '{:stack ([:constant 1] [:constant 2] [:constant 3]), :max-var 0, :max-const 4, :max-calc 0, :vars {0 0}}
        :iadd)))

(is (= '{:stack ([:calc 0] [:constant 3]), :max-var 0, :max-const 4, :max-calc 1, :vars {0 0}}
      (add-state
        '{:stack ([:constant 1] [:constant 2] [:constant 3]), :max-var 0, :max-const 4, :max-calc 0, :vars {0 0}}
        :iand)))

(is (= '{:stack ([:value -1] [:constant 3]), :max-var 0, :max-const 4, :max-calc 0, :vars {0 0}}
      (add-state
        '{:stack ([:constant 3]), :max-var 0, :max-const 4, :max-calc 0, :vars {0 0}}
        :iconst_m1)))

(defn no-redundancy?
  "Does the supplied candidate (presuming num-args arguments) contain no redundant sequence of operations?"
  [num-args s]
  (loop [remainder s state (init-state num-args)]
    (if (empty? remainder) true
      (recur (rest remainder) state))))

;(is (= true (no-redundancy? 1 '[:iload_0 :iconst_1 :iadd :ireturn])))
;(is (= false (no-redundancy? 1 '[:iload_0 :iconst_1 :iadd :ipop :ireturn])))
;(is (= true (no-redundancy? 1 '[:iload_0 :iconst_1 :istore_1 :iload_1 :ipop :ireturn])))
