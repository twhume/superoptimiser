(ns Filters.InfluenceFilter)
(use 'clojure.set)
(use 'clojure.test)
(use 'Main.Global)

; The Influence Filter tracks the flow of "influence" of arguments to the candidate sequence through to its
; eventual :ireturn. If a candidate's output does not depend on all its input, or on stack or variable entries
; which haven't been influenced by its input, then the filter returns false.

(defn has-influence?
  "Is the influence stack entry recording influence by num-args input variables?"
  [num-args entry]
  ; entry contains all (range 0 num-args)?
  (subset? (range num-args) entry))
(is (= true (has-influence? 1 #{0})))
(is (= true (has-influence? 1 #{0 1 2})))
(is (= true (has-influence? 2 #{0 1 2 3})))
(is (= false (has-influence? 1 #{1 2})))
(is (= false (has-influence? 2 #{2 3 4})))
(is (= false (has-influence? 1 #{2})))

(defn load-var-onto-stack
  "Take the value of a local variable v and push it onto the stack, all in map m"
  [m v]
  (assoc m :stack (conj (:stack m) (or (get (:vars m) v) #{v}))))

(defn load-stack-into-var
  "Take the topmost value of the stack and push into local variable v, all in map m"
  [m v]
  (assoc m
         :vars (assoc (:vars m) v (first (:stack m)))
         :stack (rest (:stack m))))

(defn combine-toptwo-stack
  "Take any influence from the top two items on the stack, combine it, and put it back, all in map m"
  [m]
  (assoc m
         :stack (conj
                  (nthrest (:stack m) 2)
                  (apply union (filter #(not(nil? %)) (take 2 (:stack m)))))))

(defn add-nil-influence-to-stack
  "Add an entry to the stack containing no influence at all"
  [m]
  (assoc m :stack (conj (:stack m) nil)))

(defn pop-from-stack
  "Pop the top n entries from the stack passed in inside m"
  [m n]
  (assoc m :stack (nthrest (:stack m) n)))

(is (= {:vars {} :stack '(#{2})} (pop-from-stack {:vars {} :stack '(#{1} #{2})} 1)))
(is (= {:vars {} :stack '()} (pop-from-stack {:vars {} :stack '(#{1} #{2})} 2)))

(defn swap-in-stack
  "Swaps the top 2 entries from the stack pass in inside m"
  [m]
  (let [stack (:stack m)]
    (assoc m :stack (concat (reverse (take 2 stack)) (nthrest stack 2)))))

(is (= {:vars {} :stack '(#{2} #{1})} (swap-in-stack {:vars {} :stack '(#{1} #{2})})))

(defn duplicate-stack
  "Handles various forms of stack duplication: operate on the top n entries, position duplicates p steps down, in map m"
  [m n p]
  (let [stack (:stack m) entries (take n stack)]
    (assoc m :stack
           (concat (take p stack) entries (nthrest stack p)))))

; unit tests for a basic DUP
(is (= {:vars {} :stack '(#{1} #{1} #{2})} (duplicate-stack {:vars {} :stack '(#{1} #{2})} 1 0)))
(is (= {:vars {} :stack '(#{2} #{2})} (duplicate-stack {:vars {} :stack '(#{2})} 1 0)))
(is (= {:vars {} :stack '(nil nil)} (duplicate-stack {:vars {} :stack '(nil)} 1 0)))
(is (= {:vars {} :stack '()} (duplicate-stack {:vars {} :stack '()} 1 0)))

; unit tests for a DUP_X1
(is (= {:vars {} :stack '(#{1} #{2} #{1})} (duplicate-stack {:vars {} :stack '(#{1} #{2})} 1 2)))
(is (= {:vars {} :stack '(#{1} #{2} #{1} #{3})} (duplicate-stack {:vars {} :stack '(#{1} #{2} #{3})} 1 2)))

; unit tests for a DUP_X2 (presuming integer arithmetic only)
(is (= {:vars {} :stack '(#{1} #{2} #{3} #{1})} (duplicate-stack {:vars {} :stack '(#{1} #{2} #{3})} 1 3)))

; unit tests for a DUP2 (presuming integer arithmetic only)
(is (= {:vars {} :stack '(#{1} #{2} #{1} #{2})} (duplicate-stack {:vars {} :stack '(#{1} #{2})} 2 0)))

; unit tests for a DUP2_X1 (presuming integer arithmetic only)
(is (= {:vars {} :stack '(#{1} #{2} #{3} #{1} #{2})} (duplicate-stack {:vars {} :stack '(#{1} #{2} #{3})} 2 3)))

; unit tests for a DUP2_X2 (presuming integer arithmetic only)
(is (= {:vars {} :stack '(#{1} #{2} #{3} #{4} #{1} #{2})} (duplicate-stack {:vars {} :stack '(#{1} #{2} #{3} #{4})} 2 4)))

(defn contains-enough-iloads?
  "Does the sequence supplied contain enough ILOAD instructions to be valid?"
  [nv l]
  (let [iload-set (set (map #(list (keyword (str "iload_" %))) (range 0 nv)))]
    (subset? iload-set (set l))))

(is (= true (contains-enough-iloads? 1 '((:iload_0) (:ireturn)))))
(is (= false (contains-enough-iloads? 1 '((:iload_1) (:ireturn)))))
(is (= false (contains-enough-iloads? 2 '((:iload_0) (:ireturn)))))
(is (= false (contains-enough-iloads? 2 '((:iload_0) (:ireturn)))))
(is (= true (contains-enough-iloads? 2 '((:iload_0) (:iload_1) (:ireturn)))))

(defn retains-influence?
  "Is the output of the sequence determined by its inputs?"
  [nv l]
  (loop [head l infl-map {:vars (into {} (map #(assoc {} % #{%}) (range 0 nv))) :stack '()}]
    (let [op (first (first head))]
      (cond
        (empty? head) true
        
        ; Dumping a local variable onto the stack:
        
        (= :iload_0 op) (recur (rest head) (load-var-onto-stack infl-map 0))
        (= :iload_1 op) (recur (rest head) (load-var-onto-stack infl-map 1))
        (= :iload_2 op) (recur (rest head) (load-var-onto-stack infl-map 2))
        (= :iload_3 op) (recur (rest head) (load-var-onto-stack infl-map 3))
        
        ; Storing the top of the stack in a local variable: pop the stack, associate the variable with the influence value at the top of the stack
        
        (= :istore_0 op) (recur (rest head) (load-stack-into-var infl-map 0))
        (= :istore_1 op) (recur (rest head) (load-stack-into-var infl-map 1))
        (= :istore_2 op) (recur (rest head) (load-stack-into-var infl-map 2))
        (= :istore_3 op) (recur (rest head) (load-stack-into-var infl-map 3))

        ; take the influence from the top 2 items on the stack, combine it, remove them, and add a new entry with this combined influence    
        
        (or
          (= :iadd op)
          (= :iand op)
          (= :idiv op)
          (= :imul op)
          (= :ior op)
          (= :ishl op)
          (= :ishr op)
          (= :isub op)
          (= :iushr op)
          (= :ixor op)
          (= :irem op)) (recur (rest head) (combine-toptwo-stack infl-map))
        
        ; push an entry into the stack marked with no influence from any variables
        (or
          (= :bipush op)
          (= :iconst_m1 op)
          (= :iconst_0 op)
          (= :iconst_1 op)
          (= :iconst_2 op)
          (= :iconst_3 op)
          (= :iconst_4 op)
          (= :iconst_5 op)) (recur (rest head) (add-nil-influence-to-stack infl-map))
  
        (= :dup op) (recur (rest head) (duplicate-stack infl-map 1 0))
        (= :dup_x1 op) (recur (rest head) (duplicate-stack infl-map 1 2))
        (= :dup_x2 op) (recur (rest head) (duplicate-stack infl-map 1 3))
        (= :dup2 op) (recur (rest head) (duplicate-stack infl-map 2 0))
        (= :dup2_x1 op) (recur (rest head) (duplicate-stack infl-map 2 3))
        (= :dup2_x2 op) (recur (rest head) (duplicate-stack infl-map 2 4))

        (= :pop op) (recur (rest head) (pop-from-stack infl-map 1))
        (= :pop2 op) (recur (rest head) (pop-from-stack infl-map 2))
        (= :swap op) (recur (rest head) (swap-in-stack infl-map))
        
        ; INEG has no effect - the topmost item retains its influence
        (= :ineg op) (recur (rest head) infl-map)
        ; IINC does the same. It'll increase values of a variable.
        (= :iinc op) (recur (rest head) infl-map)

        ; check that the item we are returning has been influenced by every input variable
        (= :ireturn op) (has-influence? nv (first (:stack infl-map)))

        ; Hit a branch? All bets are off, but check we at least have one ILOAD per argument
        (:jump (op opcodes)) (contains-enough-iloads? nv l)

        :else (do
                (println "Unhandled operation" op)
                false)))))

(is (= true (retains-influence? 1 '((:iload_0) (:ireturn)))))
(is (= false (retains-influence? 1 '((:bipush) (:ireturn)))))
(is (= false (retains-influence? 1 '((:iload_0) (:bipush) (:ireturn)))))
(is (= true (retains-influence? 1 '((:iload_0) (:bipush) (:ifle) (:ireturn)))))
(is (= true (retains-influence? 1 '((:iload_0) (:bipush) (:pop) (:ireturn)))))
(is (= true (retains-influence? 1 '((:iload_0) (:bipush) (:pop) (:ineg) (:ireturn)))))
(is (= true (retains-influence? 1 '((:iload_0) (:dup) (:pop) (:ineg) (:ireturn)))))
