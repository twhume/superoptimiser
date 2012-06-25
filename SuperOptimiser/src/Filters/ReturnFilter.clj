(ns Filters.ReturnFilter)
(use 'clojure.test)
(use 'Main.Global)

(defn no-ireturn?
  "Does the supplied sequence not include an ireturn?"
  [l]
  (nil? (some #{:ireturn} l)))

; Unit tests
(is (= true (no-ireturn? [:ixor :iushr])))
(is (= false (no-ireturn? [:ixor :ireturn ])))

(defn finishes-ireturn?
  "Does the supplied sequence finish with an ireturn?"
  [l]
  (= :ireturn (last l)))

; Unit tests
(is (= false (finishes-ireturn? [:ixor :iushr])))
(is (= false (finishes-ireturn? [:ireturn :iushr])))
(is (= true (finishes-ireturn? [:ixor :ireturn ])))