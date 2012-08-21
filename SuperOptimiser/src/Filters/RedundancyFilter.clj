(ns Filters.RedundancyFilter)
(use 'clojure.test)
(use 'Main.Global)

; The redundancy filter looks for sequences of redundant operations in a list of JVM opcodes. It does this
; by creating a data structure corresponding to the state of frame; by versioning writes to the frame;
; and by looking to see whether the overall state of a frame is unchanged between any two sets of operations.
; If a sequence of operations has not changed the state of a frame, that sequence is by definition redundant.
; If a candidate sequence of opcodes contains any redundant operations, it is by definition not optimal.

(defn init-state
  "Initialises a map for the state of the code sequence, with the number of arguments passed in"
  [num-args]
  {:stack '()        ; A structure corresponding to the operand stack of the sequence
   :max-var 1 ; The highest version number we've given to a variable
   :max-const 0      ; The highest version number we've given to a constant
   :max-calc 0       ; The highest version number we've given to a calculation
   :vars (apply hash-map (interleave (range 4) (map #(keyword (if (>= % num-args) nil (str "arg-" %))) (range 4))))})

(defn inc-max
  "Increments a max value in the state map passed in"
  [which state-map]
  (assoc state-map which (inc (which state-map))))

(defn inc-all-vars
  "Increments the variable version number for every variable being tracked"
  [state-map]
  (assoc state-map
         :vars (apply hash-map (let [i (atom (dec (:max-var state-map)))]
                                 (interleave (keys (:vars state-map)) (repeatedly #(vector :var (swap! i inc))))))
         :max-var (+ 4 (:max-var state-map))))

(defn constant?
  "Is this entry from the stack a constant?"
  [e]
  (if (vector? e) (if (= :constant (first e)) true false) false))

(is (= true (constant? '[:constant 0])))
(is (= false (constant? '[:calc 0])))

; When we add a state 

(defn add-state
  "Updates the state map with a single extra opcode"
  [state-map opcode]
  (let [stack (:stack state-map) vars (:vars state-map)]
	  (case opcode
	    :bipush (inc-max :max-const (assoc state-map :stack (conj stack [:constant (:max-const state-map)])))
      :dup (assoc state-map :stack (conj stack (first stack)))
      
      :dup_x1 (assoc state-map :stack (concat (take 2 stack) (list (first stack)) (nthrest stack 2)))
      :dup_x2 (assoc state-map :stack (concat (take 3 stack) (list (first stack)) (nthrest stack 3)))
      :dup2 (assoc state-map :stack (concat (take 2 stack) stack))
      :dup2_x1 (assoc state-map :stack (concat (take 3 stack) (take 2 stack) (nthrest stack 3)))
      :dup2_x2 (assoc state-map :stack (concat (take 4 stack) (take 2 stack) (nthrest stack 4)))
      
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
        :ishr
        :iushr
        :ishl
        :isub
        :ixor) (inc-max :max-calc (assoc state-map :stack (cons [:calc (:max-calc state-map)] (nthrest stack 2))))
      
      
      :iconst_m1 (assoc state-map :stack (cons [:value -1] stack))
      :iconst_0 (assoc state-map :stack (cons [:value 0] stack))
      :iconst_1 (assoc state-map :stack (cons [:value 1] stack))
      :iconst_2 (assoc state-map :stack (cons [:value 2] stack))
      :iconst_3 (assoc state-map :stack (cons [:value 3] stack))
      :iconst_4 (assoc state-map :stack (cons [:value 4] stack))
      :iconst_5 (assoc state-map :stack (cons [:value 5] stack))

      :istore_0 (assoc state-map :stack (rest stack) :vars (assoc vars 0 (first stack)))
      :istore_1 (assoc state-map :stack (rest stack) :vars (assoc vars 1 (first stack)))
      :istore_2 (assoc state-map :stack (rest stack) :vars (assoc vars 2 (first stack)))
      :istore_3 (assoc state-map :stack (rest stack) :vars (assoc vars 3 (first stack)))

      ; if the top item on the stack is a constant... make it into a new constant
      ; Otherwise do a new calculation
      :ineg (if (constant? (first (:stack state-map)))
              (inc-max :max-const (assoc state-map :stack (cons [:constant (:max-const state-map)] (rest stack))))
              (inc-max :max-calc (assoc state-map :stack (cons [:calc (:max-calc state-map)] (rest stack)))))
      
      :ireturn (assoc state-map :stack (rest stack))
      :iinc (inc-all-vars state-map)

      :iload_0 (assoc state-map :stack (cons (get vars 0) stack))
      :iload_1 (assoc state-map :stack (cons (get vars 1) stack))
      :iload_2 (assoc state-map :stack (cons (get vars 2) stack))
      :iload_3 (assoc state-map :stack (cons (get vars 3) stack))

              
	    )))

; Lots of unit tests...

(is (=
      '{:stack ([:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0, 1 nil, 2 nil, 3 nil}}
      (add-state (init-state 1) :bipush)))

(is (= '{:stack ([:constant 0] [:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0, 1 nil, 2 nil, 3 nil}}
       (add-state (add-state (init-state 1) :bipush) :dup)))

(is (=
      '{:stack ([:constant 1] [:constant 0][:constant 1]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0 1, nil, 2 nil, 3 nil}}
      (add-state
        '{:stack ([:constant 1] [:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0, 1 nil, 2 nil, 3 nil}}
        :dup_x1)))

(is (=
      '{:stack ([:constant 2] [:constant 1][:constant 2][:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0 1, nil, 2 nil, 3 nil}}
      (add-state
        '{:stack ([:constant 2] [:constant 1] [:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0, 1 nil, 2 nil, 3 nil}}
        :dup_x1)))

(is (=
      '{:stack ([:constant 2] [:constant 1][:constant 0][:constant 2]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0 1, nil, 2 nil, 3 nil}}
      (add-state
        '{:stack ([:constant 2] [:constant 1] [:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0, 1 nil, 2 nil, 3 nil}}
        :dup_x2)))

(is (=
      '{:stack ([:constant 3] [:constant 2] [:constant 1] [:constant 3] [:constant 2] [:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0 1, nil, 2 nil, 3 nil}}
      (add-state
        '{:stack ([:constant 3] [:constant 2] [:constant 1] [:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0, 1 nil, 2 nil, 3 nil}}
        :dup2_x1)))

(is (=
      '{:stack ([:constant 3] [:constant 2] [:constant 1] [:constant 0] [:constant 3] [:constant 2]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0 1, nil, 2 nil, 3 nil}}
      (add-state
        '{:stack ([:constant 3] [:constant 2] [:constant 1] [:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0, 1 nil, 2 nil, 3 nil}}
        :dup2_x2)))

(is (=
      '{:stack ([:constant 3] [:constant 2] [:constant 3] [:constant 2] [:constant 1] [:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0 1, nil, 2 nil, 3 nil}}
      (add-state
        '{:stack ([:constant 3] [:constant 2] [:constant 1] [:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0, 1 nil, 2 nil, 3 nil}}
        :dup2)))

(is (=
      '{:stack ([:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0 1, nil, 2 nil, 3 nil}}
      (add-state
        '{:stack ([:constant 1] [:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0, 1 nil, 2 nil, 3 nil}}
        :pop)))

(is (=
      '{:stack ([:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0}}
      (add-state
        '{:stack ([:constant 2] [:constant 1] [:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0}}
        :pop2)))

(is (=
      '{:stack ([:constant 1] [:constant 2] [:constant 3]), :max-var 1, :max-const 4, :max-calc 0, :vars {0 :arg-0}}
      (add-state
        '{:stack ([:constant 2] [:constant 1] [:constant 3]), :max-var 1, :max-const 4, :max-calc 0, :vars {0 :arg-0}}
        :swap)))

(is (= '{:stack ([:calc 0] [:constant 3]), :max-var 1, :max-const 4, :max-calc 1, :vars {0 :arg-0}}
      (add-state
        '{:stack ([:constant 1] [:constant 2] [:constant 3]), :max-var 1, :max-const 4, :max-calc 0, :vars {0 :arg-0}}
        :iadd)))

(is (= '{:stack ([:calc 0] [:constant 3]), :max-var 1, :max-const 4, :max-calc 1, :vars {0 :arg-0}}
      (add-state
        '{:stack ([:constant 1] [:constant 2] [:constant 3]), :max-var 1, :max-const 4, :max-calc 0, :vars {0 :arg-0}}
        :iand)))

(is (= '{:stack ([:value -1] [:constant 3]), :max-var 1, :max-const 4, :max-calc 0, :vars {0 :arg-0}}
      (add-state
        '{:stack ([:constant 3]), :max-var 1, :max-const 4, :max-calc 0, :vars {0 :arg-0}}
        :iconst_m1)))

(is (= '{:stack (), :max-var 1, :max-const 1, :max-calc 0, :vars {0 [:constant 0]}}
      (add-state
        '{:stack ([:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0}}
        :istore_0)))

(is (= '{:stack ([:constant 5]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 [:constant 5]}}
      (add-state
        '{:stack (), :max-var 1, :max-const 1, :max-calc 0, :vars {0 [:constant 5]}}
        :iload_0)))

(is (= '{:stack ([:constant 6]), :max-var 5, :max-const 1, :max-calc 0, :vars {0 [:var 1] 1 [:var 2] 2 [:var 3] 3 [:var 4]}}
      (add-state
        '{:stack ([:constant 6]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 [:arg-0] 1 nil 2 nil 3 nil}}
        :iinc)))

(is (=
      '{:stack ([:constant 1]), :max-var 1, :max-const 2, :max-calc 0, :vars {0 :arg-0 1, nil, 2 nil, 3 nil}}
      (add-state
        '{:stack ([:constant 0]), :max-var 1, :max-const 1, :max-calc 0, :vars {0 :arg-0, 1 nil, 2 nil, 3 nil}}
        :ineg)))

(is (=
      '{:stack ([:calc 1]), :max-var 1, :max-const 1, :max-calc 2, :vars {0 :arg-0 1, nil, 2 nil, 3 nil}}
      (add-state
        '{:stack ([:calc 0]), :max-var 1, :max-const 1, :max-calc 1, :vars {0 :arg-0, 1 nil, 2 nil, 3 nil}}
        :ineg)))

(defn state-recurred?
  "Has cur-state ever occurred before, in the list of states supplied as past-states?"
  [cur-state past-states]
  (loop [states-remainder past-states]
    (let [examined-state (first states-remainder)]
	    (cond
	      (empty? examined-state) false
	      (and
	        (= (:vars examined-state) (:vars cur-state))
	        (= (:stack examined-state) (:stack cur-state))) true
	      :else    
	       (recur (rest states-remainder))))))

(defn no-redundancy?
  "Does the supplied candidate (presuming num-args arguments) contain no redundant sequence of operations?"
  [num-args s]
  (loop [remainder s cur-state (init-state num-args) past-states '()]
    (let [cur-op (first (first remainder))]

      (cond
        (empty? remainder) true
        
        ; Found a branching instruction? If we're making a conditional branch based on 
        ; a constant at the top of the stack... fail, this is redundant and could be
        ; replaced by a GOTO. On the other hand, if we're not then all bets are off
        ; from here on, so return true
        
        (:cjump (cur-op opcodes)) (if (constant? (first (:stack cur-state))) false true) 
        (= cur-op :goto) true
        (state-recurred? cur-state past-states) false
        :else
        (recur (rest remainder) (add-state cur-state cur-op) (cons cur-state past-states))))))

(is (= true (no-redundancy? 1 '((:iload_0) (:ireturn)))))
(is (= false (no-redundancy? 1 '((:iload_0) (:iload_0) (:iload_1) (:pop2) (:ireturn)))))
(is (= true (no-redundancy? 1 '((:iload_0) (:ineg) (:ireturn)))))
(is (= true (no-redundancy? 1 '((:iload_0) (:iconst_1) (:iadd) (:ireturn)))))
(is (= false (no-redundancy? 1 '((:iload_0) (:iconst_1) (:iadd) (:pop) (:ireturn)))))
(is (= false (no-redundancy? 1 '((:iload_0) (:iconst_1) (:istore_1) (:iload_1) (:pop) (:ireturn)))))
(is (= false (no-redundancy? 1 '((:iload_0) (:istore_0) (:ireturn)))))
(is (= false (no-redundancy? 1 '((:iload_0) (:bipush) (:ifeq) (:ireturn)))))
(is (= true (no-redundancy? 1 '((:iload_0) (:bipush) (:goto) (:ireturn)))))
(is (= false (no-redundancy? 1 '((:iload_0) (:bipush) (:dup) (:ifeq) (:ireturn)))))
(is (= false (no-redundancy? 1 '((:iload_0) (:bipush) (:dup) (:pop) (:ifeq) (:ireturn)))))


