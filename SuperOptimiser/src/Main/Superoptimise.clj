(ns Main.Superoptimise)
(import 'clojure.lang.Reflector)
(use 'Main.Bytecode)
(use 'Main.Opcodes)
(use 'clojure.test)

; Main driver functions for the SuperOptimiser. Kept here so they don't pollute your individual SO stuff

(defn invoke-method [class method & arg] (Reflector/invokeStaticMethod class method (into-array arg)))

(defn num-method-args
  "How many arguments does the quoted Java method signature contain?"
  [s]
  (dec (- (.indexOf s ")") (.indexOf s "("))))

; This macro runs a piece of code with a defined timeout; I took it from
; http://stackoverflow.com/questions/6694530/executing-a-function-with-a-timeout
(defmacro with-timeout [millis opcodes & body]
    `(let [future# (future ~@body)]
      (try
          (.get future# ~millis java.util.concurrent.TimeUnit/MILLISECONDS)
        (catch java.util.concurrent.TimeoutException x# 
          (do
            (println "Terminating" ~opcodes)
            (println (future-cancel future#))
            false)))))

; Unit test to check infinite loops finish and fail
;(is (= false (with-timeout 1000 '(:a) (recur))))
;(is (= true (with-timeout 1000 '(:b) (= true true))))


; generate all 2-sequence bytecodes
; map each one to a class file
; load the class file
; pass it through the equivalence tests

(defn check-passes
  "check if a class passes its equivalence tests"
  [tests class]
  (let [num (:seq-num class)]
    (do (if (= 0 (mod num 25000)) (println num)))
      (loop [remaining-tests tests]
        (let [next-test (first remaining-tests)]
          (cond
            (empty? remaining-tests) true
            (not 
              (try
                (next-test (:class class))
                (catch Exception e (do (println "Exception" (:code class) e) false))
                (catch Error e (do (println "Error" (:code class) e) false)))) false
            :else (recur (rest remaining-tests)))))))




(defn superoptimise
  "Main driver function for the SuperOptimiser"
  [seq-len c-root m-name m-sig tests]
  (filter (partial check-passes tests)
        (map #(assoc % :class (get-class % c-root m-name m-sig))
             (expanded-numbered-opcode-sequence seq-len (num-method-args m-sig)))))

(defn superoptimise-nocheck
  "Main driver function for the SuperOptimiser - doesn't do equivalence testing, uses pmap"
  [seq-len c-root m-name m-sig tests]
        (pmap #(assoc % :class (get-class %  c-root m-name m-sig))
             (expanded-numbered-opcode-sequence seq-len (num-method-args m-sig))))

(defn superoptimise-pmap
  "Main driver function for the SuperOptimiser - using pmap"
  [seq-len c-root m-name m-sig tests]
  (filter (partial check-passes tests)
        (pmap #(assoc % :class (get-class % c-root m-name m-sig (:seq-num %)))
             (expanded-numbered-opcode-sequence seq-len (num-method-args m-sig)))))

; ---- Parallelised implementation below ----

(defn make-classes
  "Takes a sequence s, maps all its entries into classes"
  [c-root m-name m-sig tests s]
 (filter (partial check-passes tests) (map #(assoc % :class (get-class %  c-root m-name m-sig)) s))
)

(defn superoptimise-partitioned
  "Main driver function for the SuperOptimiser - with partitioned pmap and filtering"
  [seq-len c-root m-name m-sig tests partition-size]
  (apply concat
         (pmap #(make-classes c-root m-name m-sig tests %)
               (partition-all partition-size
                              (expanded-numbered-opcode-sequence seq-len (num-method-args m-sig))))))



;(apply concat (pmap #(identity %) (partition-all 10 (range 0 1000))))
