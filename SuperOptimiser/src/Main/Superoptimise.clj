(ns Main.Superoptimise
  (:import (java.util.concurrent TimeoutException TimeUnit FutureTask)))
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

; The method below was adapted from code at https://github.com/flatland/clojail/blob/master/src/clojail/core.clj#L40

(defn with-timeout
  "Take a name, function, and timeout. Run the function in a named ThreadGroup until the timeout."
  ([name code thunk time]
     (let [tg (new ThreadGroup (str name)) task (FutureTask. (comp identity thunk))
           thr (if tg (Thread. tg task) (Thread. task))]
       (try
         (.start thr)
         (.get task time TimeUnit/MILLISECONDS)
         (catch TimeoutException e
           (.cancel task true)
           (.stop thr)
           (println "Timed out" name code)
           false)
         (catch Exception e
           (.cancel task true)
           (.stop thr) 
           (println "Exception" e)
           false)
         (finally (when tg (.stop tg)))))))

; Taken from http://stackoverflow.com/questions/2622750/why-does-clojure-hang-after-having-performed-my-calculations
(defn pfilter [pred coll]
  (map second
    (filter first
      (pmap (fn [item] [(pred item) item]) coll))))

(defn check-passes-with-timeout
  "check if a class passes its equivalence tests"
  [tests class]
  (let [num (:seq-num class)]
    (do (if (= 0 (mod num 25000)) (println num)))
    (let [test-fn (fn [] (every? #(% (:class class)) tests))]
      (with-timeout num (:code class) test-fn 2000))))

(defn check-passes
  "check if a class passes its equivalence tests"
  [tests class]
  (let [num (:seq-num class)]
    (do (if (= 0 (mod num 25000)) (println num)))
    (try
      (every? #(% (:class class)) tests)
    (catch Exception e
      (println "Exception" e (:code class))
      false)
    (catch VerifyError e
	  ; ignore VerifyErrors
      false)
    (catch Error e
      (println "Error" e  (:code class))
      false))))


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
  (pfilter (partial check-passes tests)
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
