(ns Tests.ThreadingTest
    (:import (java.util.concurrent TimeoutException TimeUnit FutureTask)))

(defn run-with-timeout
	"Run the supplied code block with the timeout supplied; throw a TimeoutException if the timeout is reached, and kill any threads involved"
 [fn timeout]
 (let [thread (Thread. fn)]
   (.start thread)
   )
 )

; If the code contains a jump:
;  Create a thread which takes a function, runs it, stores its result in a value, signals back
;  Wait for this signal, or time out and kill the thread
;  Return
; Otherwise
;  Just run the damn function already


(defn test-1 [a]
  (do
    (println "test-1")
    (< a 500)))

(defn test-2 [a]
  (do
    (println "test-2")
    (> a 1)))

(defn test-3 [a]
  (do
    (println "test-3")
    (even? a)))

(def tests [test-1 test-2 test-3])
(defn all-tests [c]
  (every? #(% c) tests))

; The method below was adapted from code at https://github.com/flatland/clojail/blob/master/src/clojail/core.clj#L40

(defn with-timeout
  "Take a name, function, and timeout. Run the function in a named ThreadGroup until the timeout."
  ([name thunk time]
     (let [tg (new ThreadGroup name) task (FutureTask. (comp identity thunk))
           thr (if tg (Thread. tg task) (Thread. task))]
       (try
         (.start thr)
         (.get task time TimeUnit/MILLISECONDS)
         (catch TimeoutException e
           (.cancel task true)
           (.stop thr)
           (println "Timed out")
           false)
         (catch Exception e
           (.cancel task true)
           (.stop thr) 
           (println "Exception" e)
           false)
         (finally (when tg (.stop tg)))))))

;(def the-task (fn [] (do (println "kick-off") (loop [] (recur)))))
(def the-task (fn [] (all-tests 10)))

(with-timeout "fred"
               the-task
               2000)


