(ns Scratchpad.Scratch)
(use '[clojure.math.combinatorics])




(def replacements '(\D \E))
(def test-seq '((\A \B \C)(\A \A \B)(\C \C \C)))




(defn expand-seq
  "Expands the sequence, replacing \\C with new sequences containing \\D and \\E"
  [s]
  (let [num-tokens (count (filter #{\C} s))]
    (if (= 0 num-tokens) s
      (replace s '\C replacements)))
  
  ; count wildcards in the sequence
  ; create a combinatorial sequence of possibilities
  ; map each of these into the sequence
  ; return
)

(map expand-seq test-seq)