.data
nl: .asciiz "\n"
a: .word 0
b: .float 0.0
msg: .asciiz ""
flag: .word 0
letra: .word 0
str_0: .asciiz "string"
str_1: .asciiz "msg"
str_2: .asciiz "texto"
flt_0: .float float
flt_1: .float b
flt_2: .float y

.text
.globl main
main:
    jal NAVIDAD
    li $v0, 10
    syscall
    lw $t0, x
    li $t1, 5
    add $t2, $t0, $t1
    sw $t2, a
    lw $t0, y
    lw $t1, 3.0
    mul $t2, $t0, $t1
    sw $t2, b
    lw $t0, a
    li $t1, 10
    sgt $t2, $t0, $t1
    move $t0, $t2
    lw $t1, ok
    and $t2, $t0, $t1
    sw $t2, flag
    lw $a0, a
    li $v0, 1
    syscall
    la $a0, nl
    li $v0, 4
    syscall
    lw $a0, b
    li $v0, 1
    syscall
    la $a0, nl
    li $v0, 4
    syscall
    lw $a0, flag
    li $v0, 1
    syscall
    la $a0, nl
    li $v0, 4
    syscall
    lw $a0, c
    li $v0, 1
    syscall
    la $a0, nl
    li $v0, 4
    syscall
    lw $a0, texto
    li $v0, 1
    syscall
    la $a0, nl
    li $v0, 4
    syscall
