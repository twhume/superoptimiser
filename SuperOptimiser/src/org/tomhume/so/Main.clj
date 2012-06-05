(ns org.tomhume.so.Main)
(import 'clojure.lang.Reflector)
(use 'org.tomhume.so.Bytecode)
(use 'org.tomhume.so.Opcodes)
(use 'org.tomhume.so.TestMap)


; Main driver functions for the SuperOptimiser. Kept here so they don't pollute your individual SO stuff

(defn invoke-method [class method & arg] (Reflector/invokeStaticMethod class method (into-array arg)))

(defn num-method-args
  "How many arguments does the quoted Java method signature contain?"
  [s]
  (dec (- (.indexOf s ")") (.indexOf s "("))))

; generate all 2-sequence bytecodes
; map each one to a class file
; load the class file
; pass it through the equivalence test map

(defn check-passes
  "check if a class passes its equivalence tests"
  [tm class]
  (let [num (:seq-num class)]
    (do (if (= 0 (mod num 25000)) (println num)))
    (try (passes? tm (:class class)) (catch VerifyError e false) (catch IllegalArgumentException e2 false))))

; stick a "(do (println e)" before the false to get a log of errors - we should try and prevent these

(defn superoptimise
  "Main driver function for the SuperOptimiser"
  [seq-len c-root m-name m-sig tm]
  (filter (partial check-passes tm)
        (map #(assoc % :class (get-class (:code %)  c-root m-name m-sig))
             (expanded-numbered-opcode-sequence seq-len (num-method-args m-sig)))))
