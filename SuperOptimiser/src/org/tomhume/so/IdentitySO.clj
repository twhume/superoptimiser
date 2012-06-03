(ns org.tomhume.so.IdentitySO)
(use 'org.tomhume.so.Bytecode)
(use 'org.tomhume.so.Opcodes)
(use 'org.tomhume.so.TestMap)

; set up an equivalence test map

(def class-name-root "IdentityTest6")
(def method-name "identity")
(def method-signature "(I)I")

; generate all 2-sequence bytecodes
; map each one to a class file
; load the class file
; pass it through the equivalence test map

(map-indexed (fn [idx code] (get-class (:code code)  (str class-name-root "-" idx) method-name method-signature))
     (mapcat identity (map expand-opcodes (opcode-sequence 2)))) ; generate all 2-sequence bytecodes

