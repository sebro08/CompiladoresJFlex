.data
nl: .asciiz "\n"
x: .word 0

.text
.globl main
main:
    jal NAVIDAD
    li $v0, 10
    syscall
    lw $t0, y
    li $t1, 3
    add $t2, $t0, $t1
    sw $t2, x
    lw $a0, x
    li $v0, 1
    syscall
    la $a0, nl
    li $v0, 4
    syscall
