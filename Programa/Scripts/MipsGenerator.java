/*
import java.io.*;
import java.util.*;

public class MipsGenerator {

    private final Nodo raiz;
    private final TablaSimbolos tabla;
    private final PrintWriter out;

    private int labelCounter = 0;
    private Stack<String> breakLabels = new Stack<>();

    public MipsGenerator(Nodo raiz, TablaSimbolos tabla, String outputFile) throws IOException {
        this.raiz = raiz;
        this.tabla = tabla;
        this.out = new PrintWriter(new FileWriter(outputFile));}

    private String newLabel(String base) {
        return base + "_" + (labelCounter++);}

    public void generar() {
        writeHeader();
        generarNodo(raiz);
        writeFooter();
        out.close();}

    private void writeHeader() {
        out.println(".data");
        out.println("nl: .asciiz \"\\n\"");

        for (TablaSimbolos.SymbolInfo s : tabla.getGlobalSymbols()) {
            switch (s.tipo) {
                case "int":
                case "boolean":
                case "char":
                    out.println(s.nombre + ": .word 0");
                    break;
                case "float":
                    out.println(s.nombre + ": .float 0.0");
                    break;
                case "string":
                    out.println(s.nombre + ": .asciiz \"\"");
                    break;
                default:
                    out.println(s.nombre + ": .word 0");}}

        out.println();
        out.println(".text");
        out.println(".globl main");
        out.println("main:");}

    private void writeFooter() {
        out.println("    li $v0, 10");
        out.println("    syscall");}

    private void generarFuncion(Nodo n) {
    String nombre = n.hijos.get(1).lexema; // nombre de la función
    out.println(nombre + ":");
    // Prolog
    out.println("    addiu $sp, $sp, -8");
    out.println("    sw $ra, 4($sp)");
    out.println("    sw $fp, 0($sp)");
    out.println("    move $fp, $sp");
    generarNodo(n.hijos.get(3));     // cuerpo de la función
    // Epilog (si no hay return explícito)
    out.println("    move $sp, $fp");
    out.println("    lw $ra, 4($sp)");
    out.println("    lw $fp, 0($sp)");
    out.println("    addiu $sp, $sp, 8");
    out.println("    jr $ra");}

private void generarLlamadaFuncion(Nodo n) {
    String nombre = n.hijos.get(0).lexema;
    List<Nodo> params = n.hijos.subList(1, n.hijos.size());
    // primeros 4 parámetros en $a0-$a3
    for (int i = 0; i < Math.min(4, params.size()); i++) {
        cargar("$a" + i, params.get(i));}
    // parámetros adicionales en pila
    for (int i = 4; i < params.size(); i++) {
        cargar("$t0", params.get(i));
        out.println("    addiu $sp, $sp, -4");
        out.println("    sw $t0, 0($sp)");}

    out.println("    jal " + nombre);
    // limpiar pila si hubo parámetros adicionales
    if (params.size() > 4) {
        out.println("    addiu $sp, $sp, " + (params.size() - 4) * 4);}}

    private void generarNodo(Nodo n) {
        switch (n.lexema) {
        case "FUNCION":
            generarFuncion(n);
            break;
	case "LLAMADA_FUNCION": generarLlamadaFuncion(n); break;
        case "PROGRAMA":
        case "MAIN":
        case "BLOQUE":
        case "SENTENCIAS":
            for (Nodo h : n.hijos) generarNodo(h);
            break;
            case "ASIGNACION":
                generarAsignacion(n);
                break;
            case "SHOW":
                generarShow(n);
                break;
            case "GET":
                generarGet(n);
                break;
            case "RETURN":
                generarReturn(n);
                break;
            case "BREAK":
                generarBreak();
                break;
            case "DECIDE":
                generarDecide(n);
                break;
            case "LOOP":
                generarLoop(n);
                break;
            case "FOR":
                generarFor(n);
                break;
            //Expresiones
            case "+": case "-": case "*": case "/": case "//": case "%":
            case "^":
            case ">": case "<": case ">=": case "<=":
            case "==": case "!=":
            case "@": case "~": case "Σ":
                generarExpr(n);
                break;
            default:
                break;}}

    private void generarExpr(Nodo n) {
        Nodo izq = n.hijos.get(0);
        Nodo der = n.hijos.size() > 1 ? n.hijos.get(1) : null;
        cargar("$t0", izq);
        if (der != null) cargar("$t1", der);
        switch (n.lexema) {
            case "+": out.println("    add $t2, $t0, $t1"); break;
            case "-": out.println("    sub $t2, $t0, $t1"); break;
            case "*": out.println("    mul $t2, $t0, $t1"); break;
            case "/":
            case "//":
                out.println("    div $t0, $t1");
                out.println("    mflo $t2");
                break;
            case "%":
                out.println("    div $t0, $t1");
                out.println("    mfhi $t2");
                break;
            case "^":
                generarPotencia();
                break;
            case ">":  out.println("    sgt $t2, $t0, $t1"); break;
            case "<":  out.println("    slt $t2, $t0, $t1"); break;
            case ">=": out.println("    sge $t2, $t0, $t1"); break;
            case "<=": out.println("    sle $t2, $t0, $t1"); break;
            case "==": out.println("    seq $t2, $t0, $t1"); break;
            case "!=": out.println("    sne $t2, $t0, $t1"); break;
            case "@": out.println("    and $t2, $t0, $t1"); break;
            case "~": out.println("    or  $t2, $t0, $t1"); break;
            case "Σ": out.println("    seq $t2, $t0, $zero"); break;}}

    private void generarPotencia() {
        String Lloop = newLabel("pow_loop");
        String Lend  = newLabel("pow_end");

        out.println("    li $t2, 1");
        out.println(Lloop + ":");
        out.println("    beq $t1, $zero, " + Lend);
        out.println("    mul $t2, $t2, $t0");
        out.println("    addi $t1, $t1, -1");
        out.println("    j " + Lloop);
        out.println(Lend + ":");}

private void cargar(String reg, Nodo n) {

    if (n.hijos.isEmpty()) {
        // literal entero
        if (n.lexema.matches("-?\\d+")) {
            out.println("    li " + reg + ", " + n.lexema);
            return;}
        // identificador simple (variable)
        out.println("    lw " + reg + ", " + n.lexema);
        return;}

    // Acceso del arreglo
    if (n.lexema.equals("ACCESO_ARREGLO")) {
        String nombre = n.hijos.get(0).lexema;
        Nodo fila = n.hijos.get(1);
        Nodo col  = n.hijos.get(2);
        
        int columnas = tabla.lookup(nombre).columnas; // número de columnas (puede venir de la tabla)

        cargarElementoArreglo(nombre, fila, col, columnas);

        out.println("    move " + reg + ", $t0"); // cargarElementoArreglo deja el valor en $t0
        return;}

    generarExpr(n);
    out.println("    move " + reg + ", $t2");}

    private void generarAsignacion(Nodo n) {
        Nodo lhs = n.hijos.get(0);
        Nodo rhs = n.hijos.get(1);
        generarExpr(rhs);

        if (lhs.lexema.equals("ACCESO_ARREGLO")) {

        String nombre = lhs.hijos.get(0).lexema;
        Nodo fila = lhs.hijos.get(1);
        Nodo col  = lhs.hijos.get(2);

        int columnas = tabla.lookup(nombre).columnas;

        guardarElementoArreglo(nombre, fila, col, columnas);

        } else {
            out.println("    sw $t2, " + lhs.lexema);}}

    private void generarShow(Nodo n) {
        Nodo expr = n.hijos.get(0);
        String tipo = expr.getTipo();

        if (tipo.equals("string")) {
            out.println("    la $a0, " + expr.lexema);
            out.println("    li $v0, 4");
        } else {
            cargar("$a0", expr);
            switch (tipo) {
                case "int": case "boolean": case "char": out.println("    li $v0, 1"); break;
                case "float": out.println("    li $v0, 2"); break;}}
        out.println("    syscall");
        out.println("    la $a0, nl");
        out.println("    li $v0, 4");
        out.println("    syscall");}


    private void generarGet(Nodo n) {
        String var = n.hijos.get(0).lexema;
        String tipo = tabla.lookup(var).tipo;

        switch (tipo) {
            case "int":
            case "boolean":
            case "char":
                out.println("    li $v0, 5");
                out.println("    syscall");
                out.println("    sw $v0, " + var);
                break;

            case "float":
                out.println("    li $v0, 6");
                out.println("    syscall");
                out.println("    swc1 $f0, " + var);
                break;}}

    private void generarReturn(Nodo n) {
        cargar("$v0", n.hijos.get(0));
        out.println("    move $sp, $fp");
        out.println("    lw $ra, 4($sp)");
        out.println("    lw $fp, 0($sp)");
        out.println("    addiu $sp, $sp, 8");
        out.println("    jr $ra");}


    private void generarBreak() {
        if (!breakLabels.isEmpty()) {
            out.println("    j " + breakLabels.peek());}}

private void generarDecide(Nodo n) {

    String Lend = newLabel("decide_end");
    
    for (int i = 0; i < n.hijos.size(); i++) { // Recorre todas las condiciones

        Nodo hijo = n.hijos.get(i);
        // ELSE
        if (hijo.lexema.equals("ELSE")) {
            generarNodo(hijo.hijos.get(1)); // bloque del else
            break;}
        // condicion
        String Lnext = newLabel("cond_next");

        Nodo expr = hijo.hijos.get(0);
        Nodo bloque = hijo.hijos.get(2);

        cargar("$t0", expr);
        out.println("    beq $t0, $zero, " + Lnext);

        generarNodo(bloque);
        out.println("    j " + Lend);

        out.println(Lnext + ":");} 
        out.println(Lend + ":");}

    private void generarLoop(Nodo n) {
        String Lstart = newLabel("loop_start");
        String Lend   = newLabel("loop_end");

        breakLabels.push(Lend);

        out.println(Lstart + ":");
        generarNodo(n.hijos.get(0));
        cargar("$t0", n.hijos.get(1));
        out.println("    bne $t0, $zero, " + Lend);
        out.println("    j " + Lstart);
        out.println(Lend + ":");
        breakLabels.pop();}

    private void generarFor(Nodo n) {
        String Lstart = newLabel("for_start");
        String Lend   = newLabel("for_end");

        breakLabels.push(Lend);

        generarAsignacion(n.hijos.get(0));
        out.println(Lstart + ":");
        cargar("$t0", n.hijos.get(1));
        out.println("    beq $t0, $zero, " + Lend);
        generarNodo(n.hijos.get(3));
        generarExpr(n.hijos.get(2));
        out.println("    j " + Lstart);
        out.println(Lend + ":");
        breakLabels.pop();}

private void cargarElementoArreglo(String nombre, Nodo filaExpr, Nodo colExpr, int columnas) {
    generarExpr(filaExpr);
    out.println("    move $t1, $t0");

    generarExpr(colExpr);
    out.println("    move $t2, $t0");
    out.println("    li $t3, " + columnas);
    out.println("    mul $t1, $t1, $t3");
    out.println("    add $t1, $t1, $t2");
    out.println("    sll $t1, $t1, 2");
    out.println("    la $t4, " + nombre);
    out.println("    add $t4, $t4, $t1");
    out.println("    lw $t0, 0($t4)");}

private void guardarElementoArreglo(String nombre, Nodo filaExpr, Nodo colExpr, int columnas) {
    out.println("    move $t5, $t0"); // valor a guardar

    generarExpr(filaExpr);
    out.println("    move $t1, $t0");

    generarExpr(colExpr);
    out.println("    move $t2, $t0");
    out.println("    li $t3, " + columnas);
    out.println("    mul $t1, $t1, $t3");
    out.println("    add $t1, $t1, $t2");
    out.println("    sll $t1, $t1, 2");
    out.println("    la $t4, " + nombre);
    out.println("    add $t4, $t4, $t1");
    out.println("    sw $t5, 0($t4)");
    }
}

*/