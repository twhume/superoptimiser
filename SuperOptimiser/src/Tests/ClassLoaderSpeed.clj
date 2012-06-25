(ns Tests.ClassLoaderSpeed)
(use 'Main.Bytecode)
(import '(org.objectweb.asm ClassWriter Opcodes))
(import '(org.objectweb.asm.tree  AbstractInsnNode VarInsnNode InsnNode IincInsnNode IntInsnNode ClassNode MethodNode InsnList))


(def opcodes '[:iload_0 :ireturn])

(defn get-class-bytes-with-asm-ref
  "Creates a Java Class from the supplied data, returns an array of bytes representing that class. Input should be a map containing keys
   :length, :vars and :code, containing the number of opcodes, the max. number of local variables and a list of opcodes and arguments"

  [code className methodName methodSig cn cw mn]
  
  (let [ins (get-instructions code)]
    
    (set! (. cn version) Opcodes/V1_5)
    (set! (. cn access) Opcodes/ACC_PUBLIC)
    (set! (. cn name) className)
    (set! (. cn superName) "java/lang/Object")

    (doto (. mn instructions) .clear)
    (doto (. mn instructions) (.add ins))
    
    (if (= 0 (. (. cn methods) size))
      (doto (. cn methods) (.add mn)))
    (. cn accept cw)
    (. cw toByteArray)))


(defn test-recreate-cl-every
  "Run a test n times, reinstantiating the classloader every m times"
  [n m]
  (loop [^DynamicClassLoader cl (new clojure.lang.DynamicClassLoader) times-left n]
    (let [class-name (str "Identity-" times-left) cl-bytes (get-class-bytes opcodes class-name "identity" "(I)I")]
      (load-class cl class-name cl-bytes)
      (if (= 0 times-left) ()
        (recur (if (= 0 (mod times-left m)) (new clojure.lang.DynamicClassLoader) cl) (dec times-left))))))

(defn test-recreate-cl-every-hold-asm-refs
  "Run a test n times, reinstantiating the classloader every m times, holding onto Class-writing references"
  [n m]
  (let [cw (new ClassWriter ClassWriter/COMPUTE_MAXS)
        cn (new ClassNode)
        mn (new MethodNode (+ Opcodes/ACC_PUBLIC Opcodes/ACC_STATIC) "identity" "(I)I" nil nil)] 
                (loop [^DynamicClassLoader cl (new clojure.lang.DynamicClassLoader) times-left n]
  (let []
                  (let [class-name (str "Identity-" times-left)
                        cl-bytes (get-class-bytes-with-asm-ref opcodes class-name "identity" "(I)I" cn cw mn)]
                    (load-class cl class-name cl-bytes)
                    (. cw visitEnd)
                    (if (= 0 times-left) ()
                      (recur (if (= 0 (mod times-left m)) (new clojure.lang.DynamicClassLoader) cl) (dec times-left))))))))
