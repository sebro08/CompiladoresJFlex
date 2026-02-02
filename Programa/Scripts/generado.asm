.data
nl: .asciiz "\n"
gCounter: .word 0
gFlag: .word 0
str_0: .asciiz "OK"
str_1: .asciiz "NO OK"

.text
.globl main
main:
    jal NAVIDAD
    li $v0, 10
    syscall

sumaPot:
    # Prologo de funcion
    addiu $sp, $sp, -8
    sw $ra, 4($sp)
    sw $fp, 0($sp)
    move $fp, $sp
    # reserva local r
    addiu $sp, $sp, -4
    sw $zero, -8($fp)
    lw $t2, 8($fp)
    addiu $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t2, 12($fp)
    move $t9, $t2
    lw $t8, 0($sp)
    addiu $sp, $sp, 4
    add $t2, $t8, $t9
    sw $t2, -8($fp)
    lw $v0, -8($fp)
    j sumaPot_end_2
sumaPot_end_2:
    move $sp, $fp
    lw $fp, 0($sp)
    lw $ra, 4($sp)
    addiu $sp, $sp, 8
    jr $ra

validar:
    # Prologo de funcion
    addiu $sp, $sp, -8
    sw $ra, 4($sp)
    sw $fp, 0($sp)
    move $fp, $sp
    # reserva local ok
    addiu $sp, $sp, -4
    sw $zero, -8($fp)
    lw $t2, 8($fp)
    addiu $sp, $sp, -4
    sw $t2, 0($sp)
    li $t2, 5
    move $t9, $t2
    lw $t8, 0($sp)
    addiu $sp, $sp, 4
    sgt $t2, $t8, $t9
    addiu $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t2, gFlag
    seq $t2, $t2, $zero
    move $t9, $t2
    lw $t8, 0($sp)
    addiu $sp, $sp, 4
    and $t2, $t8, $t9
    beq $t2, $zero, decide_else_5
    li $t2, 1
    sw $t2, -8($fp)
    j decide_end_4
decide_else_5:
    li $t2, 0
    sw $t2, -8($fp)
decide_end_4:
    lw $v0, -8($fp)
    j validar_end_3
validar_end_3:
    move $sp, $fp
    lw $fp, 0($sp)
    lw $ra, 4($sp)
    addiu $sp, $sp, 8
    jr $ra

NAVIDAD:
    # Prologo de NAVIDAD
    addiu $sp, $sp, -8
    sw $ra, 4($sp)
    sw $fp, 0($sp)
    move $fp, $sp
    # reserva local a
    addiu $sp, $sp, -4
    sw $zero, -8($fp)
    # reserva local b
    addiu $sp, $sp, -4
    sw $zero, -12($fp)
    # reserva local res
    addiu $sp, $sp, -4
    sw $zero, -16($fp)
    # reserva local estado
    addiu $sp, $sp, -4
    sw $zero, -20($fp)
    li $t2, 54
    sw $t2, -8($fp)
    li $t2, 456
    sw $t2, -12($fp)
    lw $t2, -12($fp)
    addiu $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t2, -8($fp)
    addiu $sp, $sp, -4
    sw $t2, 0($sp)
    jal sumaPot
    addiu $sp, $sp, 8
    move $t2, $v0
    sw $t2, -16($fp)
    lw $a0, -16($fp)
    li $v0, 1
    syscall
    la $a0, nl
    li $v0, 4
    syscall
    lw $t2, -16($fp)
    addiu $sp, $sp, -4
    sw $t2, 0($sp)
    jal validar
    addiu $sp, $sp, 4
    move $t2, $v0
    sw $t2, -20($fp)
    lw $t2, -20($fp)
    addiu $sp, $sp, -4
    sw $t2, 0($sp)
    li $t2, 1
    move $t9, $t2
    lw $t8, 0($sp)
    addiu $sp, $sp, 4
    seq $t2, $t8, $t9
    beq $t2, $zero, decide_else_7
    la $a0, str_0
    li $v0, 4
    syscall
    la $a0, nl
    li $v0, 4
    syscall
    j decide_end_6
decide_else_7:
    la $a0, str_1
    li $v0, 4
    syscall
    la $a0, nl
    li $v0, 4
    syscall
decide_end_6:
    lw $v0, -12($fp)
    j NAVIDAD_END
NAVIDAD_END:
    move $sp, $fp
    lw $fp, 0($sp)
    lw $ra, 4($sp)
    addiu $sp, $sp, 8
    jr $ra
