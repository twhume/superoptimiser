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
  (loop [stack-size 0 op-head l ]
    (let [cur-op (first op-head) next (rest op-head)]
      (cond
        (> (:opstack-needs (cur-op opcodes)) stack-size) false
        (empty? next) true
        :else (recur (+ stack-size (:opstack-effect (cur-op opcodes))) next)))))

(is (= false (uses-operand-stack-ok? [:ixor])))
(is (= false (uses-operand-stack-ok? [:ixor :ixor])))
(is (= false (uses-operand-stack-ok? [:ireturn])))
(is (= true (uses-operand-stack-ok? [:iload_0 :ireturn])))
(is (= true (uses-operand-stack-ok? [:iload_0 :iload_0 :ixor])))
(is (= true (uses-operand-stack-ok? [:iload_0 :iload_0 :ixor :ireturn])))
(is (= false (uses-operand-stack-ok? [:iload_0 :iload_0 :ixor :ixor])))
(is (= false (uses-operand-stack-ok? [:iload_0 :iload_0 :iinc :ixor :ixor])))