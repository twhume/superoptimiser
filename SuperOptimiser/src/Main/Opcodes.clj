(ns Main.Opcodes)
(use 'clojure.set)
(use 'clojure.test)
(use 'clojure.math.combinatorics)
(use 'Main.Global)
(use '[Filters.RedundancyFilter :only (no-redundancy?)])
(use '[Filters.InfluenceFilter :only (retains-influence?)])
(use '[Filters.OperandStackFilter :only (uses-operand-stack-ok?)])
(use '[Filters.VariableUseFilter :only (uses-vars-ok?)])
(use '[Filters.ReturnFilter :only (finishes-ireturn? no-ireturn?)])
(use '[Filters.StackHeightFilter :only (branches-respect-stack-height?)])

; A list of opcodes which store into a variable. We count these so that
; we can derive a ceiling for the possible number of local variables.
(def storage-opcodes '[:istore :istore_0 :istore_1 :istore_2 :istore_3])

; Also any operation that takes 2 entries, calculates a result and is followed by a pop is redundant; could be replaced by a pop2
; and 2 constant-pushing operations followed by a pop2

(def redundant-pairs '(
                        [:swap :swap]       ; Two swaps leave things as they were
                        [:pop :pop]         ; Could be replaced by :pop2
                        [:ineg :ineg]       ; Two negations get us back where we started
                        [:iconst_0 :idiv]   ; Divide by zero, never fun
                        [:iconst_0 :irem]   ; Divide by zero, never fun
                        [:iconst_0 :iadd]   ; Adding zero does nothing
                        [:iconst_1 :imul]   ; Multiplying by 1 does nothing
                        [:iconst_1 :idiv]   ; Dividing by 1 does nothing
                        ))

(defn contains-no-redundant-pairs?
  "Does the supplied sequence contain any sequences of operations which are redundant?"
  [l]
  (let [opcodes (map first l)]
    (loop [pairs redundant-pairs]
      (if (empty? pairs) true
        (do
          (let [cur-pair (first pairs) idx-first (.indexOf opcodes (first cur-pair)) idx-next (inc idx-first)]
            (if
              (and
                (> idx-first -1)
                (< idx-first (dec (count opcodes)))
                (= (second cur-pair) (nth opcodes idx-next))) false
              (recur (rest pairs)))))))))

(is (= false (contains-no-redundant-pairs? '((:ixor) (:swap) (:swap)))))
(is (= false (contains-no-redundant-pairs? '((:swap) (:swap)))))
(is (= false (contains-no-redundant-pairs? '((:swap) (:swap) (:ixor)))))
(is (= true (contains-no-redundant-pairs? '((:ixor) (:swap) (:ixor) (:swap)))))

(defn is-valid?
  "Master validity filter: returns true if this opcode sequence can form the basis of a viable bytecode sequence"
  [n s]
  (and
    (finishes-ireturn? s)
    (uses-vars-ok? n true s)
    (uses-operand-stack-ok? s)
    (contains-no-redundant-pairs? s)
    (retains-influence? n s)
    (no-redundancy? n s)
))

(defn is-fertile?
  "Master fertility filter: returns true if any children of this opcode sequence s with n arguments may be valid"
  [n s]
  (and
    (uses-operand-stack-ok? s)
    (no-redundancy? n s)
    (no-ireturn? s)
    (uses-vars-ok? n false s)
    (contains-no-redundant-pairs? s)
))

; This version of the get-children method can be used to enforce that every code sequence starts
; by loading its argument. This is a shortcut; most of them *seem* to do this...
(defn get-children-new [n s] 
  (if (empty? s) '([(:iload_0)])
    (if (is-fertile? n s) (map #(conj s (list %)) (keys opcodes)))))

(defn get-children [n s] (if (or (empty? s) (is-fertile? n s)) (map #(conj s (list %)) (keys opcodes))))

; This version of opcode-sequence can be used to enforce the idea that every code sequence ends
; with an IRETURN and starts with an ILOAD_0. It's a shortcut designed to cut down the search space.
(defn opcode-sequence-new
  "Return a sequence of potentially valid opcode sequences N opcodes in length"
  [max-depth num-args]
  (let [validity-filter (partial is-valid? num-args) fertile-children (partial get-children-new num-args) depth (dec max-depth)]
    (filter validity-filter (map #(conj % (list :ireturn)) (rest (tree-seq #(< (count %) depth) fertile-children '[]))))))

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

(defn list-jumps
  "Examine the sequence of opcodes o passed in for jump operations, and return a map of start -> dest for them"
  [o]
  (loop [remainder o jump-map (sorted-map) pos 0]
      (if (empty? remainder) jump-map
        (let [cur (first remainder)] 
          (recur (rest remainder)
                 (if (:jump ((first cur) opcodes)) (assoc jump-map pos (+ pos (second cur))) jump-map)
                 (inc pos))))))

(is (= {1 0} (list-jumps '((:iload_0) (:ifle -1) (:ireturn)))))
(is (= {1 2} (list-jumps '((:iload_0) (:ifle 1) (:ireturn)))))
(is (= {1 0 2 0} (list-jumps '((:iload_0) (:ifle -1) (:ifle -2) (:ireturn)))))
(is (= {1 0 2 3} (list-jumps '((:iload_0) (:ifle -1) (:ifle 1) (:ireturn)))))
(is (= {1 2 2 0} (list-jumps '((:iload_0) (:ifle 1) (:ifle -2) (:ireturn)))))
(is (= {1 3 2 3} (list-jumps '((:iload_0) (:ifle 2) (:ifle 1) (:ireturn)))))

; The method expand-single-arg-partial can be renamed to substitute for expand-single-arg;
; it cuts down the overall search space by ensuring we only use specific values when expanding
; a sequence to include byte arguments

(defn expand-single-arg-partial
  "Expand a single argument to an opcode into all of its possibilities"
  [vars length position op arg]
  (cond 
        (= arg :local-var) (range 0 vars)
        (= arg :s-byte) '(-127 -64 -63 -32 -31 -16 -15 -8 -7 -4 -3 -2 -1 0 1 2 3 4 7 8 15 16 31 32 63 64 127 128)
        (= arg :us-byte) '(0 1 2 3 4 7 8 15 16 31 32 63 64 127 128 255)
        (= arg :byte) '(0 1 2 3 4 7 8 15 16 31 32 63 64 127 128 255)
        (= arg :branch-dest)  (filter #(not(< % 2)) (map #(- % position) (range 0 length)))
        :else (seq [(seq [op])])))

(defn expand-single-arg
  "Expand a single argument to an opcode into all of its possibilities"
  [vars length position op arg]
  (cond 
        (= arg :local-var) (range 0 vars)
        (= arg :s-byte) (range -127 128)
        (= arg :us-byte) (range 0 256)
        (= arg :byte) (range 0 256)
        (= arg :branch-dest)  (filter #(not(< % 2)) (map #(- % position) (range 0 length)))
        :else (seq [(seq [op])])))

(defn expand-arg
  "Returns a sequence of bytes appropriate for the (op and arguments) passed in in k and number of local variables"
  [vars length position op_args]
  (let [op (first op_args) args (rest op_args)]
    (map #(cons op %)
         (apply cartesian-product
                (map (partial expand-single-arg vars length position op) args)))))

(defn expand-opcodes
  "Take a sequence of opcodes s and expand the variables within it, returning all possibilities, presuming m arguments"
  [m s]
  (let [seq-length (count s) max-vars (+ m (count-storage-ops s)) indexing-fn (partial expand-arg max-vars seq-length)]
    (map #(hash-map :length seq-length :vars max-vars :code % :jumps (list-jumps %))
              (apply cartesian-product
                (map-indexed indexing-fn
                     (map #(cons (first %) (:args (opcodes (first %)))) s))))))

(defn expanded-numbered-opcode-sequence
  "Return a numbered, expanded sequence of all valid opcode permutations of length n presuming m arguments"
  [n m]
  (map-indexed (fn [idx itm] (assoc itm :seq-num idx))
     (filter branches-respect-stack-height?
           (mapcat identity
                   (map (partial expand-opcodes m) (opcode-sequence-new n m))))))