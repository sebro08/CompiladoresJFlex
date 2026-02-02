.data
nl: .asciiz "\n"
gCounter: .word 0
gFlag: .word 0

.text
.globl main
main:
    jal NAVIDAD
    li $v0, 10
    syscall
