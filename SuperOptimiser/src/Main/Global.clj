(ns Main.Global)

; Each opcode is a key/map pair, where the key is a Keyword, the name of the opcode
; The map contains a number of fields; compulsory ones:
;   opcode         Decimal number for the JVM opcode
; And these optional ones:
;   args           A list of any arguments this opcode needs, listed by data-type
;   opstack-needs  The number of entries on the operand stack this opcode needs to work
;   opstack-effect The effect this opcode has on the operand stack (number of entries added, negative is entries are removed)

(def opcodes {
              :bipush {:opcode 16 :args [:byte] :opstack-needs 0 :opstack-effect 1}
              
              ; operand stack-related
              
              :dup {:opcode 89 :opstack-needs 1 :opstack-effect 1}
              
              ; Because we are only dealing with integer arithmetic (category 1 computational types), we 
              ; can predict the effect of these operations on the opstack
              :dup_x1 {:opcode 90 :opstack-needs 2 :opstack-effect 1}
              :dup_x2 {:opcode 91 :opstack-needs 3 :opstack-effect 1}
              :dup2 {:opcode 92 :opstack-needs 2 :opstack-effect 2}
              :dup2_x1 {:opcode 93 :opstack-needs 3 :opstack-effect 2}
              :dup2_x2 {:opcode 94 :opstack-needs 4 :opstack-effect 2}
              
              
;             :nop {:opcode 0 :opstack-needs 0 :opstack-effect 0}
              :pop {:opcode 87 :opstack-needs 1 :opstack-effect -1}
              :pop2 {:opcode 88 :opstack-needs 2 :opstack-effect -2}
              :swap {:opcode 95 :opstack-needs 2 :opstack-effect 0}

              ; integer transformation - commented out as we aren't using other types of variables
              
;              :i2b {:opcode 145 :opstack-needs 1 :opstack-effect 0}
;              :i2c {:opcode 146 :opstack-needs 1 :opstack-effect 0}
;              :i2d {:opcode 135 :opstack-needs 1 :opstack-effect 0}
;              :i2f {:opcode 134 :opstack-needs 1 :opstack-effect 0}
;              :i2l {:opcode 133 :opstack-needs 1 :opstack-effect 0}
;              :i2s {:opcode 147 :opstack-needs 1 :opstack-effect 0}

              :iadd {:opcode 96  :opstack-needs 2 :opstack-effect -1}
              :iand {:opcode 126 :opstack-needs 2 :opstack-effect -1}
              :iconst_m1 {:opcode 2 :opstack-needs 0 :opstack-effect 1}
              :iconst_0 {:opcode 3 :opstack-needs 0 :opstack-effect 1}
              :iconst_1 {:opcode 4 :opstack-needs 0 :opstack-effect 1}
              :iconst_2 {:opcode 5 :opstack-needs 0 :opstack-effect 1}
              :iconst_3 {:opcode 6 :opstack-needs 0 :opstack-effect 1}
              :iconst_4 {:opcode 7 :opstack-needs 0 :opstack-effect 1}
              :iconst_5 {:opcode 8 :opstack-needs 0 :opstack-effect 1}
              :idiv {:opcode 108 :opstack-needs 2 :opstack-effect -1}
              
              ; branching
              
;              :if_icmpeq  {:opcode 159 :args [:us-byte, :us-byte] :opstack-needs 2 :opstack-effect -2}
;              :if_icmpne  {:opcode 160 :args [:us-byte, :us-byte] :opstack-needs 2 :opstack-effect -2}
;              :if_icmplt  {:opcode 161 :args [:us-byte, :us-byte] :opstack-needs 2 :opstack-effect -2}
;              :if_icmpge  {:opcode 162 :args [:us-byte, :us-byte] :opstack-needs 2 :opstack-effect -2}
;              :if_icmpgt  {:opcode 163 :args [:us-byte, :us-byte] :opstack-needs 2 :opstack-effect -2}
;              :if_icmple  {:opcode 164 :args [:us-byte, :us-byte] :opstack-needs 2 :opstack-effect -2}
;              :ifeq {:opcode 153 :args [:us-byte, :us-byte] :opstack-needs 1 :opstack-effect -1}
;              :ifne {:opcode 154 :args [:us-byte, :us-byte] :opstack-needs 1 :opstack-effect -1}
;              :iflt {:opcode 155 :args [:us-byte, :us-byte] :opstack-needs 1 :opstack-effect -1}
;              :ifge {:opcode 156 :args [:us-byte, :us-byte] :opstack-needs 1 :opstack-effect -1}
;              :ifgt {:opcode 157 :args [:us-byte, :us-byte] :opstack-needs 1 :opstack-effect -1}
;              :ifle {:opcode 158 :args [:us-byte, :us-byte] :opstack-needs 1 :opstack-effect -1}

              :iinc {:opcode 132 :args [:local-var, :s-byte] :opstack-needs 0 :opstack-effect 0}
;              :iload {:opcode 21 :args [:local-var] :opstack-needs 0 :opstack-effect 1}
              
              ; Commented out as these are just shortcuts for iload
              
              :iload_0 {:opcode 26 :opstack-needs 0 :opstack-effect 1}
              :iload_1 {:opcode 27 :opstack-needs 0 :opstack-effect 1}
              :iload_2 {:opcode 28 :opstack-needs 0 :opstack-effect 1}
              :iload_3 {:opcode 29 :opstack-needs 0 :opstack-effect 1}
              :imul {:opcode 104 :opstack-needs 2 :opstack-effect -1}
              :ineg {:opcode 116 :opstack-needs 1 :opstack-effect 0}
              :ior {:opcode 128 :opstack-needs 2 :opstack-effect -1}
              :irem {:opcode 112 :opstack-needs 2 :opstack-effect -1}
              :ireturn {:opcode 172 :opstack-needs 1 :opstack-effect 0}
              :ishl {:opcode 120 :opstack-needs 2 :opstack-effect -1}
              :ishr {:opcode 122 :opstack-needs 2 :opstack-effect -1}
;              :istore {:opcode 54 :args [:local-var] :opstack-needs 1 :opstack-effect -1}
              
              ; TOODO ARGH. FORGOT TO TAKE INTO ACCOUNT POPPING OFF STACK UNTIL HERE - RECHECK ABOVE ENTRIES
 
              ; Commented out as these are just shortcuts for istore
              
              :istore_0 {:opcode 59 :opstack-needs 1 :opstack-effect -1}
              :istore_1 {:opcode 60 :opstack-needs 1 :opstack-effect -1}
              :istore_2 {:opcode 61 :opstack-needs 1 :opstack-effect -1}
              :istore_3 {:opcode 62 :opstack-needs 1 :opstack-effect -1}
              :isub {:opcode 100 :opstack-needs 2 :opstack-effect -1}
              :iushr {:opcode 124 :opstack-needs 2 :opstack-effect -1}
              :ixor {:opcode 130 :opstack-needs 2 :opstack-effect -1}})

