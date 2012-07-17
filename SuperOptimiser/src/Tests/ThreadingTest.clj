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
    false))

(defn test-2 [a]
  (do
    (println "test-2")
    false))

(defn test-3 [a]
  (do
    (println "test-3")
    false))

(def tests [test-1 test-2 test-3])
(defn all-tests [c]
  (every? #(% c) tests))

; Below methods taken from https://github.com/flatland/clojail/blob/master/src/clojail/core.clj#L40

(def ^{:doc "Create a map of pretty keywords to ugly TimeUnits"}
  uglify-time-unit
  (into {} (for [[enum aliases] {TimeUnit/NANOSECONDS [:ns :nanoseconds]
                                 TimeUnit/MICROSECONDS [:us :microseconds]
                                 TimeUnit/MILLISECONDS [:ms :milliseconds]
                                 TimeUnit/SECONDS [:s :sec :seconds]}
                 alias aliases]
             {alias enum})))

(defn thunk-timeout
  "Takes a function and an amount of time to wait for the function to finish
   executing. The sandbox can do this for you. unit is any of :ns, :us, :ms,
   or :s which correspond to TimeUnit/NANOSECONDS, MICROSECONDS, MILLISECONDS,
   and SECONDS respectively."
  ([thunk ms]
     (thunk-timeout thunk ms :ms)) ; Default to milliseconds, because that's pretty common.
  ([thunk time unit]
     (thunk-timeout thunk time unit identity))
  ([thunk time unit transform]
     (thunk-timeout thunk time unit identity nil))
  ([thunk time unit transform tg]
     (let [task (FutureTask. (comp transform thunk))
           thr (if tg (Thread. tg task) (Thread. task))]
       (try
         (.start thr)
         (.get task time (or (uglify-time-unit unit) unit))
         (catch TimeoutException e
           (.cancel task true)
           (.stop thr) 
           (throw (TimeoutException. "Execution timed out.")))
         (catch Exception e
           (.cancel task true)
           (.stop thr) 
           (throw e))
         (finally (when tg (.stop tg)))))))

(defn timedfunction [] (recur))
(thunk-timeout timedfunction 1000 :ms identity (new ThreadGroup "fred"))