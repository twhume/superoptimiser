(ns Main.Superoptimise
  (:use [clojure.tools.logging :only (info error)])
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
; We don't actually use it, as we removed backwards branching mid-project

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
; A cheap means of doing our filtering by spreading the load across many threads
(defn pfilter [pred coll]
  (map second
    (filter first
      (pmap (fn [item] [(pred item) item]) coll))))

(defn check-passes-with-timeout
  "check if a class passes its equivalence tests"
  [tests class]
  (let [num (:seq-num class)]
    (let [test-fn (fn [] (every? #(% (:class class)) tests))]
      (with-timeout num (:code class) test-fn 2000))))

(defn check-passes
  "check if a class passes its equivalence tests"
  [tests c-root m-name m-sig cmap]
  (let [num (:seq-num cmap) class (get-class cmap c-root m-name m-sig (:seq-num cmap))]
    (try
      (every? #(% class) tests)
    (catch ArithmeticException e
      ; ignore these. We get so many they don't help us, and they're all divide-by-zeros
      false)
    (catch Exception e
      (error "Exception" e cmap)
      false)
    (catch VerifyError e
      (error "VerifyError" e cmap)
      false)
    (catch Error e
      (error "Error" e  cmap)
      false))))

(defn superoptimise-pmap
  "Main driver function for the SuperOptimiser - using pmap"
  [seq-len c-root m-name m-sig tests]
  (map #(info (str "PASS " c-root "." m-name " " %)) 
       (pfilter (partial check-passes tests c-root m-name m-sig)
              (expanded-numbered-opcode-sequence seq-len (num-method-args m-sig)))))

(defn superoptimise-slice
  "Main driver function for the SuperOptimiser - but just taking a slice of the overall search space"
  [seq-len c-root m-name m-sig tests num-nodes node-num]
  (map #(info (str "PASS " c-root "." m-name " " %)) 
       (pfilter (partial check-passes tests c-root m-name m-sig)
                (take-nth num-nodes
                          (drop node-num
                                (expanded-numbered-opcode-sequence seq-len (num-method-args m-sig)))))))
