(ns org.tomhume.so.Opcodes)
(use 'clojure.test)
(use 'clojure.math.combinatorics)
(use 'org.tomhume.so.TestMap)

; Each opcode is a key/map pair, where the key is a Keyword, the name of the opcode
; The map contains a number of fields; compulsory ones:
;   opcode         Decimal number for the JVM opcode
; And these optional ones:
;   args           A list of any arguments this opcode needs, listed by data-type
;   opstack-needs  The number of entries on the operand stack this opcode needs to work
;   opstack-effect The effect this opcode has on the operand stack (number of entries added, negative is entries are removed)

(def opcodes {
              :bipush {:opcode 16 :args [:byte] :opstack-needs 0 :opstack-effect 1}
              
              ; operand stack-related
              
              :dup {:opcode 89 :opstack-needs 1 :opstack-effect 1}
              
              ; TODO: I'm not sure that the opstack-needs value of 2 is right here. Needs a test
              :dup_x1 {:opcode 90 :opstack-needs 2 :opstack-effect 1}
              ; TODO: this one might have an opstack-needs of 3 in some situations... look into it
              :dup_x2 {:opcode 91 :opstack-needs 2 :opstack-effect 1}
              :dup2 {:opcode 92 :opstack-needs 1 :opstack-effect 1}
              :dup2_x1 {:opcode 93 :opstack-needs 2 :opstack-effect 1}
              :dup2_x2 {:opcode 94 :opstack-needs 2 :opstack-effect 1}
              :nop {:opcode 0}
              :pop {:opcode 87 :opstack-needs 1 :opstack-effect -1}
              :pop2 {:opcode 88 :opstack-needs 2 :opstack-effect -2}
              :swap {:opcode 95 :opstack-needs 2 :opstack-effect 0}

              ; integers
              
              :i2b {:opcode 145 :opstack-needs 1 :opstack-effect 0}
              :i2c {:opcode 146 :opstack-needs 1 :opstack-effect 0}
              :i2d {:opcode 135 :opstack-needs 1 :opstack-effect 0}
              :i2f {:opcode 134 :opstack-needs 1 :opstack-effect 0}
              :i2l {:opcode 133 :opstack-needs 1 :opstack-effect 0}
              :i2s {:opcode 147 :opstack-needs 1 :opstack-effect 0}
              :iadd {:opcode 96  :opstack-needs 2 :opstack-effect -1}
              :iand {:opcode 126 :opstack-needs 2 :opstack-effect -1}
              :iconst_m1 {:opcode 2 :opstack-needs 0 :opstack-effect 1}
              :iconst_0 {:opcode 3 :opstack-needs 0 :opstack-effect 1}
              :iconst_1 {:opcode 4 :opstack-needs 0 :opstack-effect 1}
              :iconst_2 {:opcode 5 :opstack-needs 0 :opstack-effect 1}
              :iconst_3 {:opcode 6 :opstack-needs 0 :opstack-effect 1}
              :iconst_4 {:opcode 7 :opstack-needs 0 :opstack-effect 1}
              :iconst_5 {:opcode 8 :opstack-needs 0 :opstack-effect 1}
              :idiv {:opcode 108 :opstack-needs 2 :opstack-effect -1}
              
              ; branching
              
;              :if_icmpeq  {:opcode 159 :args [:us-byte, :us-byte] :opstack-needs 2 :opstack-effect -2}
;              :if_icmpne  {:opcode 160 :args [:us-byte, :us-byte] :opstack-needs 2 :opstack-effect -2}
;              :if_icmplt  {:opcode 161 :args [:us-byte, :us-byte] :opstack-needs 2 :opstack-effect -2}
;              :if_icmpge  {:opcode 162 :args [:us-byte, :us-byte] :opstack-needs 2 :opstack-effect -2}
;              :if_icmpgt  {:opcode 163 :args [:us-byte, :us-byte] :opstack-needs 2 :opstack-effect -2}
;              :if_icmple  {:opcode 164 :args [:us-byte, :us-byte] :opstack-needs 2 :opstack-effect -2}
;              :ifeq {:opcode 153 :args [:us-byte, :us-byte] :opstack-needs 1 :opstack-effect -1}
;              :ifne {:opcode 154 :args [:us-byte, :us-byte] :opstack-needs 1 :opstack-effect -1}
;              :iflt {:opcode 155 :args [:us-byte, :us-byte] :opstack-needs 1 :opstack-effect -1}
;              :ifge {:opcode 156 :args [:us-byte, :us-byte] :opstack-needs 1 :opstack-effect -1}
;              :ifgt {:opcode 157 :args [:us-byte, :us-byte] :opstack-needs 1 :opstack-effect -1}
;              :ifle {:opcode 158 :args [:us-byte, :us-byte] :opstack-needs 1 :opstack-effect -1}

              :iinc {:opcode 132 :args [:local-var, :s-byte]}
              :iload {:opcode 21 :args [:local-var] :opstack-needs 0 :opstack-effect 1}
              :iload_0 {:opcode 26 :opstack-needs 0 :opstack-effect 1}
              :iload_1 {:opcode 27 :opstack-needs 0 :opstack-effect 1}
              :iload_2 {:opcode 28 :opstack-needs 0 :opstack-effect 1}
              :iload_3 {:opcode 29 :opstack-needs 0 :opstack-effect 1}
              :imul {:opcode 104 :opstack-needs 2 :opstack-effect -1}
              :ineg {:opcode 116 :opstack-needs 1 :opstack-effect 0}
              :ior {:opcode 128 :opstack-needs 2 :opstack-effect -1}
              :irem {:opcode 112 :opstack-needs 2 :opstack-effect -1}
              :ireturn {:opcode 172 :opstack-needs 1 :opstack-effect 0}
              :ishl {:opcode 120 :opstack-needs 2 :opstack-effect -1}
              :ishr {:opcode 122 :opstack-needs 2 :opstack-effect -1}
              :istore {:opcode 54 :args [:local-var] :opstack-needs 1 :opstack-effect -1}
              
              ; TOODO ARGH. FORGOT TO TAKE INTO ACCOUNT POPPING OFF STACK UNTIL HERE - RECHECK ABOVE ENTRIES
              
              :istore_0 {:opcode 59 :opstack-needs 1 :opstack-effect -1}
              :istore_1 {:opcode 60 :opstack-needs 1 :opstack-effect -1}
              :istore_2 {:opcode 61 :opstack-needs 1 :opstack-effect -1}
              :istore_3 {:opcode 62 :opstack-needs 1 :opstack-effect -1}
              :isub {:opcode 100 :opstack-needs 2 :opstack-effect -1}
              :iushr {:opcode 124 :opstack-needs 2 :opstack-effect -1}
              :ixor {:opcode 130 :opstack-needs 2 :opstack-effect -1}})

; A list of opcodes which store into a variable. We count these so that
; we can derive a ceiling for the possible number of local variables.
(def storage-opcodes '[:istore :istore_0 :istore_1 :istore_2 :istore_3])

(defn has-ireturn?
  "Does the supplied sequence include an ireturn?"
  [l]
  (not (nil? (some #{:ireturn} l))))

; Unit tests
(is (= false (has-ireturn? [:ixor :iushr])))
(is (= true (has-ireturn? [:ixor :ireturn ])))

(defn uses-operand-stack-ok?
  "Does the supplied sequence read from the operand stack only when there's sufficient entries in it?"
  [l]
  ; keep reading entries until you hit a jump (at which point all bets are off, return true)
  ; keep a count of the opstack-effect values so far
  ; if this is ever less than the current opstack-needs, return false
  
  true)

(defn uses-local-variables-ok?
  "Does the supplied sequence try to read from local variables only after they're written to?"
  [l]
  true)

; add a filter to check for really obvious redundancy (e.g. ireturn not final in a sequence w/o jumps)

; TBD:
; are there are least 2 different opcodes in the sequence?
; are there no reads from the operand stack before it has been written to?
; are there no reads from a local variable before it has been written to?

(def opcode-sequence-filter (test-map [has-ireturn? uses-operand-stack-ok? uses-local-variables-ok?]))

(defn opcode-sequence
  "Return a sequence of potentially valid opcode sequences N opcodes in length"
  [n]
  (filter #(passes? opcode-sequence-filter %)
         (apply cartesian-product (repeat n (keys opcodes)))))

(defn count-storage-ops
  "Count the number of operations writing to a local variable in the supplied sequence"
  [s]
  (count (filter #(some #{%} storage-opcodes) s)))

(is (= 0 (count-storage-ops [:ixor :iushr])))
(is (= 1 (count-storage-ops [:ixor :istore])))
(is (= 2 (count-storage-ops [:ixor :istore :istore])))
(is (= 2 (count-storage-ops [:ixor :istore_0 :istore])))
(is (= 2 (count-storage-ops [:ixor :istore_0 :istore :ixor])))

(defn expand-arg
  "Returns a sequence of bytes appropriate for the keyword passed in and number of local variables"
  [vars k]
  (cond 
    (= k :local-var) (range 0 vars)
    (= k :s-byte) (range -127 128)
    (= k :us-byte) (range 0 256)
    (= k :byte) (range 0 256)
    :else (seq [k])))

(is (= '(0 1 2 3 4) (expand-arg 5 :local-var)))
(is (= nil) (expand-arg 1 :dummy-keyword))

(defn expand-opcodes
  "Take a sequence of opcodes and expand the variables within it, returning all possibilities"
  [s]
  (let [seq-length (count s) max-vars (count-storage-ops s)]

    ; Expand all the arguments in the sequence into sequences of possible values,
    ; get the Cartesian product of the resulting sequence (i.e. all its possibilities)
    ; and put that into a hash, keeping the sequence length and maximum number of variables handy
    
    (map #(hash-map :length seq-length :vars max-vars :code % )
              (apply cartesian-product
                (map (partial expand-arg max-vars) 
                     (flatten (map #(cons % (:args (opcodes %))) s)))))))

; The below WORKS!
;(mapcat identity (map expand-opcodes '((:istore :istore)(:ixor :ireturn))))

(defn expanded-numbered-opcode-sequence
  "Return a numbered, expanded sequence of all valid opcode permutations of length n"
  [n]
  (map-indexed (fn [idx itm] (assoc itm :seq-num idx)) (mapcat identity (map expand-opcodes (opcode-sequence n)))))
  
(expanded-numbered-opcode-sequence 2)
