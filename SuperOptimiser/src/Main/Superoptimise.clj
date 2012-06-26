(ns Main.Superoptimise)
(import 'clojure.lang.Reflector)
(use 'Main.Bytecode)
(use 'Main.Opcodes)
(use 'Util.TestMap)

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
    (try (passes? tm (:class class)) (catch Exception e (do (println (:code class) e) false)) (catch Error e (do (println (:code class) e) false)))))

; stick a "(do (println e)" before the false to get a log of errors - we should try and prevent these

(defn superoptimise
  "Main driver function for the SuperOptimiser"
  [seq-len c-root m-name m-sig tm]
  (filter (partial check-passes tm)
        (map #(assoc % :class (get-class (:code %)  c-root m-name m-sig))
             (expanded-numbered-opcode-sequence seq-len (num-method-args m-sig)))))

(defn superoptimise-nocheck
  "Main driver function for the SuperOptimiser - doesn't do equivalence testing, uses pmap"
  [seq-len c-root m-name m-sig tm]
        (pmap #(assoc % :class (get-class (:code %)  c-root m-name m-sig))
             (expanded-numbered-opcode-sequence seq-len (num-method-args m-sig))))

(defn superoptimise-pmap
  "Main driver function for the SuperOptimiser - using pmap"
  [seq-len c-root m-name m-sig tm]
  (filter (partial check-passes tm)
        (pmap #(assoc % :class (get-class (:code %)  c-root m-name m-sig (:seq-num %)))
             (expanded-numbered-opcode-sequence seq-len (num-method-args m-sig)))))

; ---- Parallelised implementation below ----

(defn make-classes
  "Takes a sequence s, maps all its entries into classes"
  [c-root m-name m-sig tm s]
 (filter (partial check-passes tm) (map #(assoc % :class (get-class (:code %)  c-root m-name m-sig)) s))
)

(defn superoptimise-partitioned
  "Main driver function for the SuperOptimiser - with partitioned pmap and filtering"
  [seq-len c-root m-name m-sig tm partition-size]
  (apply concat
         (pmap #(make-classes c-root m-name m-sig tm %)
               (partition-all partition-size
                              (expanded-numbered-opcode-sequence seq-len (num-method-args m-sig))))))



;(apply concat (pmap #(identity %) (partition-all 10 (range 0 1000))))