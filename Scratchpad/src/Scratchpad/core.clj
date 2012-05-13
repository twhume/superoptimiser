(ns Scratchpad.core)
(require '[clojure.math.combinatorics])
 
; 91
(def alphabet (map char (concat (range 65 67))))

(defn alpha-seq
 ""
 [a b]
 (if (< (count a) b)
 (let [children (map #(str a %) alphabet)]
 (lazy-cat children (mapcat #(alpha-seq % b) children)))
))
; if which > range.length return
; else  
  
  
  
(defn make-tree
  ""
  [node l depth]
   (if (< (count node) depth)
     (let [children (map #(str node %) alphabet)]
       (lazy-cat children (mapcat #(make-tree % l depth) children) l)
       )))
     
     
;  take children of this node
; add each child to the sequence
; add each of their childre
; add children of this node to the sequence

; (NODE, SEQUENCE)
; add children of NODE to SEQUENCE
; recur with each child

;
;""
;A B C
;A B C AA AB AC

;A B C AA AB AC BA BB BC CA CB CC AAA AAB AAC ABA ABB ABC ...