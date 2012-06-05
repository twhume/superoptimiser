(ns org.tomhume.so.LoadManyClasses)
(use 'org.tomhume.so.Bytecode)

(defn test-class
  "Makes class number n"
  [n]
  (let [class (get-class '(:iload 0 :ireturn) "Identity" "identity" "(I)I")]
    (do
      (if (= 0 (mod n 1000)) (println n))
      true)))

(time (dotimes [n 100000] (test-class n)))
;(time (dorun (map #(test-class %) (range 0 100000))))