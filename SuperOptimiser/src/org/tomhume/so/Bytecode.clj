(ns org.tomhume.so.Bytecode)
(use 'clojure.test)
(import '(org.tomhume.so.Opcodes))
(import '(java.io FileOutputStream))
(import '(org.objectweb.asm ClassWriter Opcodes))
(import '(org.objectweb.asm.tree  AbstractInsnNode VarInsnNode InsnNode IincInsnNode IntInsnNode ClassNode MethodNode InsnList))

; This package handles the creation of Java class files.

(defn add-opcode
  "Creates a child of an AbstractInsNode and returns it"
  [op & argseq]
  (let [args (flatten argseq) opcode ((opcodes op) :opcode)]
  (cond
    (nil? ((opcodes op) :args)) (new InsnNode opcode)
    (= :istore op) (new VarInsnNode opcode (first args))
    (= :iload op) (new VarInsnNode opcode (first args))
    (= :iinc op) (new IincInsnNode (first args) (second args)) 
    (= :bipush op) (new IntInsnNode opcode (first args)) 
    :else nil)))

(defn add-opcode-and-args
  "Pulls an opcode off the sequence provided, adds it and any arguments to the insnlist, returns the remainder of the sequence"
  [insnlist ocs]
  (let [op (first ocs)]
    (. insnlist add (add-opcode op (rest ocs)))
    (nthrest ocs (+ 1 (count ((opcodes op) :args))))))

(defn get-instructions
  "Turns the supplied list of opcodes and arguments into an InsnList"
  [a]
  (let [l (new InsnList)]
    (loop [codes a]
      (if (empty? codes) l
        (recur (add-opcode-and-args l codes))))))

(is (= 2 (. (get-instructions '(:iload 0 :ireturn)) size)))
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

;(write-bytes "/tmp/Identity.class" (get-class-bytes '(:iload 0 :ireturn) "IdentityTest" "identity" "(I)I"))
;(. (get-instructions '(:pop :istore 1 :ireturn)) size)
