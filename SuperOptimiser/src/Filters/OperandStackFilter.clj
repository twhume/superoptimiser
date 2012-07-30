(ns Filters.OperandStackFilter)
(use 'clojure.test)
(use 'Main.Global)

; The OperandStackFilter checks for stack underflows: it runs through the operations in a candidate sequence,
; looks to see how many stack entries they require, and returns false if they ever require more entries on the
; stack than can currently be available.

(defn uses-operand-stack-ok?
  "Does the supplied sequence read from the operand stack only when there's sufficient entries in it?"
  [l]
  ; keep reading entries until you hit a jump (at which point all bets are off, return true)
  ; keep a count of the opstack-effect values so far
  ; if this is ever less than the current opstack-needs, return false
  (loop [stack-size 0 op-head l]
    (let [cur-op (first op-head) opcode (first cur-op) next (rest op-head)]
      (cond

        (> (:opstack-needs (opcode opcodes)) stack-size) false

        ; hit a jump? We used to presume all bets are off here; but now I follow straight through, so at least one path is tested
        ; In Opcodes/branches-respect-stack-height? a separate test checks that branch destinations and fall-throughs have the same
        ; stack size, so this should be safe.
;        (:jump (opcode opcodes)) true
        
        (empty? next) true
        :else (recur (+ stack-size (:opstack-effect (opcode opcodes))) next)))))

(is (= false (uses-operand-stack-ok? '((:ixor)))))
(is (= false (uses-operand-stack-ok? '((:ixor) (:ixor)))))
(is (= false (uses-operand-stack-ok? '((:ireturn)))))
(is (= true (uses-operand-stack-ok? '((:iload_0) (:ireturn)))))
(is (= true (uses-operand-stack-ok? '((:iload_0) (:iload_0) (:ixor)))))
(is (= true (uses-operand-stack-ok? '((:iload_0) (:iload_0) (:ixor) (:ireturn)))))
(is (= false (uses-operand-stack-ok? '((:iload_0) (:iload_0) (:ixor) (:ixor)))))
(is (= false (uses-operand-stack-ok? '((:iload_0) (:iload_0) (:ixor) (:ifle) (:ixor)))))
(is (= false (uses-operand-stack-ok? '((:iload_0) (:iload_0) (:iinc) (:ixor) (:ixor)))))
