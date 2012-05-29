(ns org.tomhume.so.Bytecode)
(import '(java.io FileOutputStream))
(import '(org.objectweb.asm ClassWriter Opcodes))
(import '(org.objectweb.asm.tree AbstractInsnNode ClassNode MethodNode InsnList))

; This package handles the creation of Java class files.

(defn add-opcode
  "Pulls an opcode off the sequence provided, adds it and any arguments to the insnlist, returns the remainder of the sequence"
  [insnlist opcodes]
  (let [op (first opcodes)]
    (case op
      :pop (do (println "pop!") (. insnlist (rest opcodes))
      :istore (do (println "istore!") (nthrest opcodes 2))
      :ireturn (do (println "return!") (rest opcodes))
      ((println "unknown/in bad place!") (rest opcodes)))))

(defn get-instructions
  "Turns the supplied list of opcodes and arguments into an InsnList"
  [a]
  (let [l (new InsnList)]
    (loop [codes a]
      (if (empty? codes) l
        (recur (add-opcode l codes))))))

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
    (. cw toByteArray)
  )
)

(defn write-bytes
  "Write a byte array to the filename specified"
  [fn b]
    (with-open [out (FileOutputStream. fn)]
      (.write out b)))

;(write-bytes "/tmp/Identity.class" (get-class-bytes {} "IdentityTest" "identity" "(I)I"))
(get-instructions '(:pop :istore :arg :ireturn))