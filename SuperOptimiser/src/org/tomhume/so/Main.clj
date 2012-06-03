(ns org.tomhume.so.Main)
(import 'clojure.lang.Reflector)
(use 'org.tomhume.so.Bytecode)
(use 'org.tomhume.so.Opcodes)
(use 'org.tomhume.so.TestMap)


; Main driver functions for the SuperOptimiser. Kept here so they don't pollute your individual SO stuff

(defn invoke-method [class method arg] (Reflector/invokeStaticMethod class method (into-array [arg])))

(defn num-method-args
  "How many arguments does the quoted Java method signature contain?"
  [s]
  (dec (- (.indexOf s ")") (.indexOf s "("))))

; generate all 2-sequence bytecodes
; map each one to a class file
; load the class file
; pass it through the equivalence test map

(defn superoptimise
  "Main driver function for the SuperOptimiser"
  [seq-len c-root m-name m-sig tm]
  (filter #(try (passes? tm (:class %)) (catch VerifyError e (do println e) false))
        (map #(assoc % :class (get-class (:code %)  (str c-root "-" (:seq-num %)) m-name m-sig))
             (expanded-numbered-opcode-sequence seq-len (num-method-args m-sig)))))
