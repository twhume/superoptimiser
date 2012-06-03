(ns org.tomhume.so.IdentitySO)
(use 'org.tomhume.so.Bytecode)
(use 'org.tomhume.so.Opcodes)
(use 'org.tomhume.so.TestMap)

; set up an equivalence test map

; generate all 2-sequence bytecodes
; map each one to a class file
; load the class file
; pass it through the equivalence test map

(count (map #(get-class (:code %)  "IdentityTest-1" "identity" "(I)I")
     (mapcat identity (map expand-opcodes (opcode-sequence 2))))) ; generate all 2-sequence bytecodes