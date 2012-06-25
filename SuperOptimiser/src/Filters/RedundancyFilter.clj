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

(defn inc-max-const
  "Increments the :max-const value in the state map passed in"
  [state-map]
  (assoc state-map :max-const (inc (:max-const state-map))))

; When we add a state 

(defn add-state
  "Updates the state map with a single extra opcode"
  [state-map opcode]
  (let [stack (:stack state-map)]
	  (case opcode
	    :bipush (inc-max-const (assoc state-map :stack (conj stack [:constant (:max-const state-map)])))
	    )))

(defn no-redundancy?
  "Does the supplied candidate (presuming num-args arguments) contain no redundant sequence of operations?"
  [num-args s]
  (loop [remainder s state (init-state num-args)]
    (if (empty? remainder) true
      (recur (rest remainder) state))))

(is (= true (no-redundancy? 1 '[:iload_0 :iconst_1 :iadd :ireturn])))
(is (= false (no-redundancy? 1 '[:iload_0 :iconst_1 :iadd :ipop :ireturn])))
(is (= true (no-redundancy? 1 '[:iload_0 :iconst_1 :istore_1 :iload_1 :ipop :ireturn])))
