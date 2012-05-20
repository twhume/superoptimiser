(ns Scratchpad.bytecode)
(use '[Scratchpad.test_map])
(use '[Scratchpad.Opcodes])
(use 'clojure.test)

; A set of definitions and functions for generating sequences of bytecode, filtering
; candidate sequences and otherwise manipulating them




(defn expand-arg
  "Takes a single argument and returns all its possibilities"
  [a]
  (cond (= a :us-byte) (range 0 255)
   )
)

; Generate a list of candidate sequences

; Remove any which don't pass our lists of tests
(def opcode-sequence-filter (test-map opcode_predicates))

; A sequence of 3600 2-long bytecode sequences
(def opcodes-src (apply cartesian-product (repeat 2 (keys opcodes))))



(println (count opcodes-src))
(println (count opcodes-src))

(count (filter #(passes? opcode-sequence-filter %) opcodes-src))

;(def opcodes-filtered (filter (passes? opcode-sequence-filter opcodes-src))