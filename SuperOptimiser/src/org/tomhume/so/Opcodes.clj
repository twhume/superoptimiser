(ns org.tomhume.so.Opcodes)
(use 'clojure.test)
(use 'clojure.math.combinatorics)

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
              
              ; Because we are only dealing with integer arithmetic (category 1 computational types), we 
              ; can predict the effect of these operations on the opstack
              :dup_x1 {:opcode 90 :opstack-needs 2 :opstack-effect 1}
              :dup_x2 {:opcode 91 :opstack-needs 3 :opstack-effect 1}
              :dup2 {:opcode 92 :opstack-needs 2 :opstack-effect 2}
              :dup2_x1 {:opcode 93 :opstack-needs 3 :opstack-effect 2}
              :dup2_x2 {:opcode 94 :opstack-needs 4 :opstack-effect 2}
              
              
;             :nop {:opcode 0 :opstack-needs 0 :opstack-effect 0}
              :pop {:opcode 87 :opstack-needs 1 :opstack-effect -1}
              :pop2 {:opcode 88 :opstack-needs 2 :opstack-effect -2}
              :swap {:opcode 95 :opstack-needs 2 :opstack-effect 0}

              ; integer transformation - commented out as we aren't using other types of variables
              
;              :i2b {:opcode 145 :opstack-needs 1 :opstack-effect 0}
;              :i2c {:opcode 146 :opstack-needs 1 :opstack-effect 0}
;              :i2d {:opcode 135 :opstack-needs 1 :opstack-effect 0}
;              :i2f {:opcode 134 :opstack-needs 1 :opstack-effect 0}
;              :i2l {:opcode 133 :opstack-needs 1 :opstack-effect 0}
;              :i2s {:opcode 147 :opstack-needs 1 :opstack-effect 0}

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

              :iinc {:opcode 132 :args [:local-var, :s-byte] :opstack-needs 0 :opstack-effect 0}
;              :iload {:opcode 21 :args [:local-var] :opstack-needs 0 :opstack-effect 1}
              
              ; Commented out as these are just shortcuts for iload
              
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
;              :istore {:opcode 54 :args [:local-var] :opstack-needs 1 :opstack-effect -1}
              
              ; TOODO ARGH. FORGOT TO TAKE INTO ACCOUNT POPPING OFF STACK UNTIL HERE - RECHECK ABOVE ENTRIES
 
              ; Commented out as these are just shortcuts for istore
              
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

(defn no-ireturn?
  "Does the supplied sequence not include an ireturn?"
  [l]
  (nil? (some #{:ireturn} l)))

; Unit tests
(is (= true (no-ireturn? [:ixor :iushr])))
(is (= false (no-ireturn? [:ixor :ireturn ])))

(defn finishes-ireturn?
  "Does the supplied sequence finish with an ireturn?"
  [l]
  (= :ireturn (last l)))

; Unit tests
(is (= false (finishes-ireturn? [:ixor :iushr])))
(is (= false (finishes-ireturn? [:ireturn :iushr])))
(is (= true (finishes-ireturn? [:ixor :ireturn ])))

(def redundant-pairs '(
                        [:swap :swap]       ; Two swaps leave things as they were
                        [:pop :pop]         ; Could be replaced by :pop2
                        [:ineg :ineg]       ; Two negations get us back where we started
                        [:iconst_0 :idiv]   ; Divide by zero, never fun
                        [:iconst_0 :irem]   ; Divide by zero, never fun
                        ))

(defn contains-no-redundant-pairs?
  "Does the supplied sequence contain any sequences of operations which are redundant?"
  [l]
  (loop [pairs redundant-pairs]
    (if (empty? pairs) true
      (do
        (let [cur-pair (first pairs) idx-first (.indexOf l (first cur-pair)) idx-next (inc idx-first)]
        (if
          (and
            (> idx-first -1)
            (< idx-first (dec (count l)))
            (= (second cur-pair) (nth l idx-next))) false
          (recur (rest pairs))))))))

(is (= false (contains-no-redundant-pairs? '[:ixor :swap :swap])))
(is (= false (contains-no-redundant-pairs? '[:swap :swap])))
(is (= false (contains-no-redundant-pairs? '[:swap :swap :ixor])))
(is (= true (contains-no-redundant-pairs? '[:ixor :swap :ixor :swap])))

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

(defn update-varmap
  "Takes a sequence starting with an opcode and followed by arguments, returns nil or an updated key/value pair for a hash"
  [s]
  (let [op (first s)]
	  (cond
	    (= op :iload_0) '(0 :read)
	    (= op :iload_1) '(1 :read)
	    (= op :iload_2) '(2 :read)
	    (= op :iload_3) '(3 :read)
      (= op :iload) (seq [(nth s 1) :read])
	    (= op :istore_0) '(0 :write)
	    (= op :istore_1) '(1 :write)
	    (= op :istore_2) '(2 :write)
	    (= op :istore_3) '(3 :write)
      (= op :istore) (seq [(nth s 1) :write])
      :else nil)))

(is (= '(0 :write) (update-varmap '[:istore_0])))
(is (= '(3 :read) (update-varmap '[:iload_3])))
(is (= '(7 :write) (update-varmap '[:istore 7])))
(is (= '(12 :read) (update-varmap '[:iload 12])))

(defn uses-vars-ok?
  "Does the supplied sequence try to read from local variables only after they're written to, and not overwrite values in variables?"
  [nv l]
  (let [initial-hash (into {} (map #(assoc {} (identity %) :write) (range 0 nv)))]
    (loop [head l last-op initial-hash]
      (let [op (first head) vm-update (update-varmap head)]
	      (cond
	        (empty? head) true
         
         ; If we're reading from a variable which has never been written, fail the sequence
         
	        (and (= op :iload_0) (= nil (get last-op 0))) false 
	        (and (= op :iload_1) (= nil (get last-op 1))) false
	        (and (= op :iload_2) (= nil (get last-op 2))) false
	        (and (= op :iload_3) (= nil (get last-op 3))) false
          (and (= op :iload) (= nil (get last-op (nth head 1)))) false
          
         ; handle :iload
         
         ; If we're writing from a variable which we last wrote to (i.e. overwriting data), fail the sequence
	        (and (= op :istore_0) (= :write (get last-op 0))) false
	        (and (= op :istore_1) (= :write (get last-op 1))) false
	        (and (= op :istore_2) (= :write (get last-op 2))) false
	        (and (= op :istore_3) (= :write (get last-op 3))) false
          (and (= op :istore) (= :write (get last-op (nth head 1)))) false
         
         ; otherwise record the read-write state; skip the appropriate number of instructions; carry on
          
	        :else (if (= nil vm-update) (recur (rest head) last-op)
                 (recur (rest head) (assoc last-op (nth vm-update 0) (nth vm-update 1)))))))))

(is (= true (uses-vars-ok? 0 [:ixor])))
(is (= false (uses-vars-ok? 0 [:iload_0])))
(is (= true (uses-vars-ok? 0 [:istore_0 :iload_0])))
(is (= true (uses-vars-ok? 0 [:istore_0 :iload_0])))
(is (= false (uses-vars-ok? 0 [:istore_1 :iload_0])))
(is (= false (uses-vars-ok? 0 [:istore_0 :istore_0])))
(is (= true (uses-vars-ok? 0 [:istore_0 :istore_1])))
(is (= true (uses-vars-ok? 0 [:istore_0 :iload_0 :istore_0])))
(is (= true (uses-vars-ok? 0 [:istore_0 :iload_0 :istore_0 :iload_0 :iload_0])))
(is (= false (uses-vars-ok? 0 [:istore_0 :iload_1 :istore_0])))
(is (= true (uses-vars-ok? 1 [:iload_0])))
(is (= false (uses-vars-ok? 1 [:bipush :iload_3 :ireturn])))

(defn is-valid?
  "Master validity filter: returns true if this opcode sequence can form the basis of a viable bytecode sequence"
  [n s]
  (and
    (finishes-ireturn? s)
    (uses-vars-ok? n s)
    (uses-operand-stack-ok? s)
    (contains-no-redundant-pairs? s)
))

(defn is-fertile?
  "Master fertility filter: returns true if any children of this opcode sequence s with n arguments may be valid"
  [n s]
  (and
    (no-ireturn? s)
    (uses-vars-ok? n s)
    (uses-operand-stack-ok? s)
    (contains-no-redundant-pairs? s)))

(defn get-children [n s] (if (or (empty? s) (is-fertile? n s)) (map #(conj s %) (keys opcodes))))

(defn opcode-sequence
  "Return a sequence of potentially valid opcode sequences N opcodes in length"
  [max-depth num-args]
  (let [validity-filter (partial is-valid? num-args) fertile-children (partial get-children num-args)]
    (filter validity-filter (rest (tree-seq #(< (count %) max-depth) fertile-children '[])))))

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
  "Take a sequence of opcodes s and expand the variables within it, returning all possibilities, presuming m arguments"
  [m s]
  (let [seq-length (count s) max-vars (+ m (count-storage-ops s))]
    
    (map #(hash-map :length seq-length :vars max-vars :code % )
              (apply cartesian-product
                (map (partial expand-arg max-vars) 
                     (flatten (map #(cons % (:args (opcodes %))) s)))))))

(defn expanded-numbered-opcode-sequence
  "Return a numbered, expanded sequence of all valid opcode permutations of length n presuming m arguments"
  [n m]
  (map-indexed (fn [idx itm] (assoc itm :seq-num idx))
               (mapcat identity
                       (map (partial expand-opcodes m) (opcode-sequence n m)))))


