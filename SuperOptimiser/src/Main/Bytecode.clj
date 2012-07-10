(ns Main.Bytecode)
(use 'clojure.test)
(use 'Main.Global)
(import '(clojure.lang DynamicClassLoader))
(import '(java.io FileOutputStream))
(import '(org.objectweb.asm ClassWriter Opcodes))
(import '(org.objectweb.asm.tree  AbstractInsnNode VarInsnNode InsnNode IincInsnNode JumpInsnNode IntInsnNode ClassNode MethodNode InsnList LabelNode))

; This package handles the creation of Java class files.

(defn instantiate-classloader
  "Returns a new instance of the Class Loader - used to recreate it when necessary"
  [cur]
  (new clojure.lang.DynamicClassLoader))

(def classloader (atom (new clojure.lang.DynamicClassLoader)))

(defn is-jump?
  "Is the operation passed in one which triggers a jump?"
  [op]
  (or
    (= op :goto)
    (= op :if_icmpeq)
    (= op :if_icmpne)
    (= op :if_icmplt)
    (= op :if_icmpge)
    (= op :if_icmpgt)
    (= op :if_icmple)
    (= op :ifeq)
    (= op :ifne)
    (= op :iflt)
    (= op :ifge)
    (= op :ifgt)
    (= op :ifle)))

(defn add-opcode
  "Creates a child of an AbstractInsNode and returns it"
  [op labels & argseq]
  (let [args (flatten argseq)
        opcode ((opcodes op) :opcode)]
  (cond
    (= :istore op) (new VarInsnNode opcode (first args))
    (= :istore_0 op) (new VarInsnNode 54 0)
    (= :istore_1 op) (new VarInsnNode 54 1)
    (= :istore_2 op) (new VarInsnNode 54 2)
    (= :istore_3 op) (new VarInsnNode 54 3)
    (= :iload op) (new VarInsnNode opcode (first args))
    (= :iload_0 op) (new VarInsnNode 21 0)
    (= :iload_1 op) (new VarInsnNode 21 1)
    (= :iload_2 op) (new VarInsnNode 21 2)
    (= :iload_3 op) (new VarInsnNode 21 3)
    (= :iinc op) (new IincInsnNode (first args) (second args)) 
    (= :bipush op) (new IntInsnNode opcode (first args)) 
    
    ; Look up any label node from the labels map passed in
    (re-find #"^label_" (name op)) (get labels op) 
    
    ; TODO add comparison and goto support here
    
    (is-jump? op) (new JumpInsnNode opcode ((first args) labels))
    
    (nil? ((opcodes op) :args)) (new InsnNode opcode)
    :else nil)))

(defn add-opcode-and-args
  "Pulls an opcode off the sequence provided, adds it and any arguments to the insnlist, returns the remainder of the sequence"
  [insnlist ocs labels]
  (let [op (first ocs)]
    (. insnlist add (add-opcode op labels (rest ocs)))
    (nthrest ocs (+ 1 (count ((opcodes op) :args))))))

(defn insert-at
  "Create a new sequence consisting of the input sequence s with an extra item i inserted at a position distance d from p"
  [s i p d]
  (let [jump-dest (+ p d)]
        (concat (take jump-dest s) (list i) (nthrest s jump-dest))))

(is (= '(:a :b :c :1 :d :e) (insert-at '(:a :b :c :d :e) :1 3 0)))
(is (= '(:a :b :1 :c :d :e) (insert-at '(:a :b :c :d :e) :1 3 -1)))
(is (= '(:a :1 :b :c :d :e) (insert-at '(:a :b :c :d :e) :1 3 -2)))
(is (= '(:a :b :c :d :1 :e) (insert-at '(:a :b :c :d :e) :1 3 1)))
(is (= '(:a :b :c :d :e :1) (insert-at '(:a :b :c :d :e) :1 3 2)))
(is (= '(:1 :a :b :c :d :e) (insert-at '(:a :b :c :d :e) :1 0 0)))
(is (= '(:a :1 :b :c :d :e) (insert-at '(:a :b :c :d :e) :1 0 1)))
(is (= '(:a :b :1 :c :d :e) (insert-at '(:a :b :c :d :e) :1 0 2)))
(is (= '(:a :b :c :d :1 :e) (insert-at '(:a :b :c :d :e) :1 4 0)))
(is (= '(:a :b :c :d :e :1) (insert-at '(:a :b :c :d :e) :1 4 1)))
(is (= '(:a :b :c :1 :d :e) (insert-at '(:a :b :c :d :e) :1 4 -1)))
(is (= '(:a :b :1 :c :d :e) (insert-at '(:a :b :c :d :e) :1 4 -2)))
 
(defn replace-at
  "Create a new sequence consisting of the input sequence s with the item at position p replaced by i"
  [s i p]
  (concat (take p s) (list i) (nthrest s (inc p))))

(is (= '(:1 :b :c :d :e) (replace-at '(:a :b :c :d :e) :1 0)))
(is (= '(:a :1 :c :d :e) (replace-at '(:a :b :c :d :e) :1 1)))
(is (= '(:a :b :1 :d :e) (replace-at '(:a :b :c :d :e) :1 2)))
(is (= '(:a :b :c :1 :e) (replace-at '(:a :b :c :d :e) :1 3)))
(is (= '(:a :b :c :d :1) (replace-at '(:a :b :c :d :e) :1 4)))

(defn update-labelling
  "Return a modified version of sequence s such that it includes a label i at offset d from position p, and points to that label"
  [s i p d]
  (if (> (+ p d) (+ p 1)) (replace-at (insert-at s i (+ 1 p) d) i (+ 1 p))
    (replace-at (insert-at s i p d) i (+ 2 p))))

(is (= '(:a :b :1 :c :d :e :1) (update-labelling '(:a :b :c :d :e -2) :1 4 -2)))
(is (= '(:a :b :c :1 :d :e :1) (update-labelling '(:a :b :c :d :e -2) :1 4 -1)))
(is (= '(:a :b :c :d :1 :e :1) (update-labelling '(:a :b :c :d :e -2) :1 4 0)))

(is (= '(:1 :a :b :c :1 :d :e) (update-labelling '(:a :b :c -2 :d :e) :1 2 -2)))
(is (= '(:a :1 :b :c :1 :d :e) (update-labelling '(:a :b :c -2 :d :e) :1 2 -1)))
(is (= '(:a :b :1 :c :1 :d :e) (update-labelling '(:a :b :c -2 :d :e) :1 2 0)))
(is (= '(:a :b :c :1 :1 :d :e) (update-labelling '(:a :b :c -2 :d :e) :1 2 1)))
(is (= '(:a :b :c :1 :d :1 :e) (update-labelling '(:a :b :c -2 :d :e) :1 2 2)))

(is (= '(:1 :a :1 :b :c :d :e) (update-labelling '(:a 1 :b :c :d :e) :1 0 0)))
(is (= '(:a :1 :1 :b :c :d :e) (update-labelling '(:a 1 :b :c :d :e) :1 0 1)))
(is (= '(:a :1 :b :1 :c :d :e) (update-labelling '(:a 1 :b :c :d :e) :1 0 2)))
(is (= '(:a :1 :b :c :1 :d :e) (update-labelling '(:a 1 :b :c :d :e) :1 0 3)))

(defn add-labels
  "Takes a list of opcodes and arguments and adds label entries to correspond to branch destinations"
  [a]
  (loop [input a output a jump-num 0 pos 0]
    (let [cur (first input)]
      (cond
        (empty? input) output
        (is-jump? cur) (let [label-key (keyword (str "label_" jump-num))]
                         (recur (nthrest input 2)
                              (update-labelling output label-key pos (second input))
                              (inc jump-num)
                              (inc pos)))
        :else (recur (rest input) output jump-num (inc pos))))))
  ; loop through the list
  ; if we have a branch instruction, insert a label for the branch at the appropriate place, insert a reference to this label, and continue
  ; otherwise move to the next instruction

(is (= '(:label_0 :iload_0 :goto :label_0 :ireturn) (add-labels '(:iload_0 :goto -1 :ireturn))))
(is (= '(:iload_0 :goto :label_0 :label_0 :istore_1 :ireturn) (add-labels '(:iload_0 :goto 1 :istore_1 :ireturn))))
(is (= '(:iload_0 :goto :label_0 :istore_1 :label_0 :ireturn) (add-labels '(:iload_0 :goto 2 :istore_1 :ireturn))))

(defn make-labels-map
  "Take the sequence of opcodes provided and make a map of name to LabelNode, for each label"
  [o]
  (into {} (map #(assoc {} % (new LabelNode))
                (distinct
                  (filter #(re-find #"^label_" (name %)) o)))))

(defn get-instructions
  "Turns the supplied list of opcodes and arguments into an InsnList"
  [a]
  (let [l (new InsnList) labelled-opcodes (add-labels a) labels-map (make-labels-map labelled-opcodes)]
    (loop [codes labelled-opcodes]
      (if (empty? codes) l
        (recur (add-opcode-and-args l codes labels-map))))))

(is (= 2 (. (get-instructions '(:iload_0 :ireturn)) size)))
(is (= 1 (. (get-instructions '(:ireturn)) size)))

(defn get-class-bytes
  "Creates a Java Class from the supplied data, returns an array of bytes representing that class. Input should be a map containing keys
   :length, :vars and :code, containing the number of opcodes, the max. number of local variables and a list of opcodes and arguments"

  [code className methodName methodSig]
  (let [cn (new ClassNode)
        cw (new ClassWriter ClassWriter/COMPUTE_MAXS)
        mn (new MethodNode (+ Opcodes/ACC_PUBLIC Opcodes/ACC_STATIC) methodName methodSig nil nil)
        ins (get-instructions code)]
    (set! (. cn version) Opcodes/V1_5)
    (set! (. cn access) Opcodes/ACC_PUBLIC)
    (set! (. cn name) className)
    (set! (. cn superName) "java/lang/Object")
    
    (doto (. mn instructions) (.add ins))
    (doto (. cn methods) (.add mn))
    (. cn accept cw)
    (. cw toByteArray)))

(defn write-bytes
  "Write a byte array to the filename specified"
  [fn b]
    (with-open [out (FileOutputStream. fn)]
      (.write out b)))

(defn load-class
  "Load a class of the given name from the given bytecode"
  ([name bytecode cl]
      (.defineClass cl name bytecode '()))
  )


(defn get-class
  "Creates and loads a class file with the given name"
  [code className methodName methodSig seqnum]
    (try
      (let [full-class-name (str className "-" seqnum)]
        (load-class full-class-name
                    (get-class-bytes code full-class-name methodName methodSig)
                    (if (= 0 (mod seqnum 50000)) (swap! classloader instantiate-classloader) @classloader)))
      (catch ClassFormatError cfe nil)))

;(ns-unmap 'org.tomhume.so.Bytecode 'f1)
;(ns-unmap 'org.tomhume.so.Bytecode 'f2)
