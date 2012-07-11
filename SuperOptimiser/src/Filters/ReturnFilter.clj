(ns Filters.ReturnFilter)
(use 'clojure.test)
(use 'Main.Global)

; The ReturnFilter does very simple checking indeed, on the presence or absence of an :ireturn operation

(defn no-ireturn?
  "Does the supplied sequence not include an ireturn?"
  [l]
  (nil? (some #(= :ireturn (first %)) l)))

; Unit tests
(is (= true (no-ireturn? '((:ixor) (:iushr)))))
(is (= false (no-ireturn? '((:ixor) (:ireturn)))))

(defn finishes-ireturn?
  "Does the supplied sequence finish with an ireturn?"
  [l]
  (= :ireturn (first (last l))))

; Unit tests
(is (= false (finishes-ireturn? '((:ixor) (:iushr)))))
(is (= false (finishes-ireturn? '((:ireturn) (:iushr)))))
(is (= true (finishes-ireturn? '((:ixor) (:ireturn)))))