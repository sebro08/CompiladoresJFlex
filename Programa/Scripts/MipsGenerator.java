import java.io.*;
import java.util.*;

public class MipsGenerator {

    // ____________________________ ATRIBUTOS ____________________________ 

    private final Nodo raiz;
    private final TablaSimbolos tabla;
    private final PrintWriter out;

    private String currentFunctionEnd;

    private final Map<String, String> stringLabels = new HashMap<>();
    private final Map<String, String> floatLabels  = new HashMap<>();

    private int labelCounter = 0;
    private final Stack<String> breakLabels = new Stack<>();

    private boolean hasNavidad = false;

    private Map<String, Integer> offsetLocales = new HashMap<>();
    private Map<String, Integer> offsetParametros = new HashMap<>();

    private int offsetLocalActual;


    // ____________________________ CONSTRUCTOR ____________________________ 

    public MipsGenerator(Nodo raiz, TablaSimbolos tabla, String outputFile) throws IOException {
        this.raiz  = raiz;
        this.tabla = tabla;
        this.out   = new PrintWriter(new FileWriter(outputFile));
    }

    // ____________________________ MÉTODOS GENERALES ____________________________ 

    private String newLabel(String base) {
        return base + "_" + (labelCounter++);
    }

    public void generar() {
        writeHeader();
        generarNodo(raiz);

        if (!hasNavidad) {
            throw new RuntimeException(
                "Error semántico: el programa no contiene el bloque obligatorio COAL NAVIDAD()"
            );
        }
        out.close();
    }

    // ____________________________ CABECERA / PIE ____________________________ 

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
                    out.println(s.nombre + ": .word 0");
            }
        }

        registrarStrings(raiz);
        registrarFloats(raiz);

        out.println();
        out.println(".text");
        out.println(".globl main");
        out.println("main:");
        out.println("    jal NAVIDAD");
        out.println("    li $v0, 10");
        out.println("    syscall");
    }

    // ____________________________ GENERACIÓN DE NODOS ____________________________ 

    private void generarNodo(Nodo n) {
    
        if (n == null) return;

        switch (n.lexema) {

            case "FUNCION":
                generarFuncion(n);
                break;
            case "llamada":
                generarLlamadaFuncion(n);
                break;
            case "PROGRAMA":
                for (Nodo h : n.hijos) generarNodo(h);
            break;
            case "MAIN":
                hasNavidad = true;
                for (Nodo h : n.hijos) generarNodo(h);
            break;
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

            case "DECL_LOCAL":
                String nombre = n.hijos.get(1).lexema;
                offsetLocalActual -= 4;
                offsetLocales.put(nombre, offsetLocalActual);
                out.println("    # reserva local " + nombre);
                out.println("    addiu $sp, $sp, -4");
                break;

            // ===== EXPRESIONES ===== 
            case "+": case "-": case "*": case "/": case "//": case "%":
            case "^":
            case ">": case "<": case ">=": case "<=":
            case "==": case "!=":
            case "@": case "~": case "Σ":
                generarExpr(n);
                break;
        }
    }

    // ____________________________ FUNCIONES ____________________________ 

    private void generarFuncion(Nodo n) {

        String nombre = n.hijos.get(1).lexema;
        Nodo params = n.hijos.get(2);
        Nodo bloque = n.hijos.get(3);

        currentFunctionEnd = newLabel(nombre + "_end");

        out.println(nombre + ":");
        out.println("    # Prologo de funcion");
        out.println("    addiu $sp, $sp, -8");
        out.println("    sw $ra, 4($sp)");
        out.println("    sw $fp, 0($sp)");
        out.println("    move $fp, $sp");

        //  Inicializar frame 
        offsetLocales.clear();
        offsetParametros.clear();
        offsetLocalActual = -4;

        //  Registrar parámetros 
        int offsetParam = 8; // después de $fp y $ra

        for (Nodo h : params.hijos) {
            if (h.lexema.equals("itemParams")) {
                for (Nodo p : h.hijos) {
                    if (!p.lexema.equals(",") && !p.lexema.equals("¿") && !p.lexema.equals("?")) {
                        offsetParametros.put(p.lexema, offsetParam);
                        offsetParam += 4;
                    }
                }
            }
        }

        //  Generar bloque 
        generarNodo(bloque);

        // ==== Epílogo 
        out.println(currentFunctionEnd + ":");
        out.println("    move $sp, $fp");
        out.println("    lw $fp, 0($sp)");
        out.println("    lw $ra, 4($sp)");
        out.println("    addiu $sp, $sp, 8");
        out.println("    jr $ra");

        currentFunctionEnd = null;
    }

    private void generarLlamadaFuncion(Nodo n) {

        String nombre = n.hijos.get(0).lexema;
        List<Nodo> params = new ArrayList<>();

        // Extraer argumentos reales
        if (n.hijos.size() > 1) {
            Nodo args = n.hijos.get(1);
            for (Nodo h : args.hijos) {
                if (!h.lexema.equals(",")) {
                    params.add(h);
                }
            }
        }

        int stackParams = 0;

        // Parámetros extra por stack
        for (int i = params.size() - 1; i >= 4; i--) {
            Nodo p = params.get(i);
            generarExpr(p);
            out.println("    addiu $sp, $sp, -4");
            out.println("    sw $t2, 0($sp)");
            stackParams++;
        }

        // Primeros 4 parámetros
        for (int i = 0; i < params.size() && i < 4; i++) {
            Nodo p = params.get(i);
            if (p.getTipo().equals("float")) {
                out.println("    # parametro float en $f12");
                cargarFloat("$f12", p);
            } else {
                cargar("$a" + i, p);
            }
        }

        out.println("    jal " + nombre);

        // Resultado de la llamada
        TablaSimbolos.SymbolInfo f = tabla.lookup(nombre);
        if (f != null && !f.tipo.equals("float")) {
            out.println("    move $t2, $v0");
        }

        // Limpiar stack
        if (stackParams > 0) {
            out.println("    addiu $sp, $sp, " + (stackParams * 4));
        }
    }

    private String direccion(String nombre) {

        TablaSimbolos.SymbolInfo s = tabla.lookup(nombre);

        if (s == null) return nombre; // seguridad

        // Variables globales → etiqueta directa
        if (s.categoria.equals("global")) {
            return nombre;
        }

        // Variables locales → offset negativo desde $fp
        if (offsetLocales.containsKey(nombre)) {
            return offsetLocales.get(nombre) + "($fp)";
        }

        // Parámetros → offset POSITIVO desde $fp
        if (s.categoria.equals("parametro")) {
            int offset = offsetParametros.get(nombre);
            return offset + "($fp)";
        }

        return nombre;
    }

    // ____________________________ EXPRESIONES ____________________________ 

    private void generarExpr(Nodo n) {

        Nodo izq = n.hijos.get(0);
        Nodo der = n.hijos.size() > 1 ? n.hijos.get(1) : null;

        // FLOAT 
        if (n.getTipo().equals("float")) {

            cargarFloat("$f1", izq);
            if (der != null) cargarFloat("$f2", der);

            switch (n.lexema) {
                case "+": out.println("    add.s $f0, $f1, $f2"); break;
                case "-": out.println("    sub.s $f0, $f1, $f2"); break;
                case "*": out.println("    mul.s $f0, $f1, $f2"); break;
                case "/": out.println("    div.s $f0, $f1, $f2"); break;
                case "<":  out.println("    c.lt.s $f1, $f2"); generarResultadoFloatCmp(); break;
                case ">":  out.println("    c.le.s $f2, $f1"); generarResultadoFloatCmp(); break;
                case "<=": out.println("    c.le.s $f1, $f2"); generarResultadoFloatCmp(); break;
                case ">=": out.println("    c.lt.s $f2, $f1"); generarResultadoFloatCmp(); break;
                case "==": out.println("    c.eq.s $f1, $f2"); generarResultadoFloatCmp(); break;
                case "!=": out.println("    c.eq.s $f1, $f2"); generarResultadoFloatCmpInvertido(); break;
            }
            return;
        }

        // ENTEROS  
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
            case "^":  generarPotencia(); break;
            case ">":  out.println("    sgt $t2, $t0, $t1"); break;
            case "<":  out.println("    slt $t2, $t0, $t1"); break;
            case ">=": out.println("    sge $t2, $t0, $t1"); break;
            case "<=": out.println("    sle $t2, $t0, $t1"); break;
            case "==": out.println("    seq $t2, $t0, $t1"); break;
            case "!=": out.println("    sne $t2, $t0, $t1"); break;
            case "@":  out.println("    and $t2, $t0, $t1"); break;
            // AND (@) y OR (~) se implementan como operaciones lógicas sin corto circuito.
            case "~":  out.println("    or  $t2, $t0, $t1"); break;
            // Los operandos se normalizan a 0/1 en el análisis semántico.
            case "Σ":  out.println("    seq $t2, $t0, $zero"); break;
            case "++": {
                Nodo var = n.hijos.get(0);
                cargar("$t0", var);
                out.println("    addi $t0, $t0, 1");
                out.println("    sw $t0, " + direccion(var.lexema));
                out.println("    move $t2, $t0");
                break;
            }
            case "--": {
                Nodo var = n.hijos.get(0);
                cargar("$t0", var);
                out.println("    addi $t0, $t0, -1");
                out.println("    sw $t0, " + direccion(var.lexema));
                out.println("    move $t2, $t0");
                break;
            }
        }
    }
    // ____________________________ POTENCIA ____________________________ 

    private void generarPotencia() {

        String Lloop = newLabel("pow_loop");
        String Lend  = newLabel("pow_end");

        out.println("    li $t2, 1");
        out.println(Lloop + ":");
        out.println("    beq $t1, $zero, " + Lend);
        out.println("    mul $t2, $t2, $t0");
        out.println("    addi $t1, $t1, -1");
        out.println("    j " + Lloop);
        out.println(Lend + ":");
    }

    // ____________________________ CARGA DE VALORES ____________________________ 

    private void cargar(String reg, Nodo n) {

        // Float
        if (n.getTipo().equals("float")) {
            cargarFloat(reg.replace("$t", "$f"), n);
            return;
        }

        // Literal o variable simple
        if (n.hijos.isEmpty()) {

            if (n.lexema.matches("-?\\d+")) {
                out.println("    li " + reg + ", " + n.lexema);
                return;
            }

            out.println("    lw " + reg + ", " + direccion(n.lexema));
            return;
        }

        // Acceso a arreglo
        if (n.lexema.contains("Filas")) {

            String nombre = n.hijos.get(0).lexema;
            Nodo fila = n.hijos.get(1);
            Nodo col  = n.hijos.get(2);

            int columnas = tabla.lookup(nombre).columnasArreglo;

            cargarElementoArreglo(nombre, fila, col, columnas);
            out.println("    move " + reg + ", $t0");
            return;
        }

        // Expresión
        generarExpr(n);
        out.println("    move " + reg + ", $t2");
    }

    // ____________________________ ASIGNACIÓN ____________________________ 

    private void generarAsignacion(Nodo n) {

        Nodo lhs = n.hijos.get(0);
        Nodo rhs = n.hijos.get(1);

        generarExpr(rhs);

        // Asignación a arreglo
        if (lhs.lexema.contains("Filas")) {

            String nombre = lhs.hijos.get(0).lexema;
            Nodo fila = lhs.hijos.get(1);
            Nodo col  = lhs.hijos.get(2);

            int columnas = tabla.lookup(nombre).columnasArreglo;

            guardarElementoArreglo(nombre, fila, col, columnas);

        } else {
            out.println("    sw $t2, " + direccion(lhs.lexema));
        }
    }

    // ____________________________ ENTRADA / SALIDA ____________________________ 

    private void generarShow(Nodo n) {

        Nodo expr = n.hijos.get(0);
        String tipo = expr.getTipo();

        if (tipo.equals("string")) {
            out.println("    la $a0, " + stringLabels.get(expr.lexema));
            out.println("    li $v0, 4");
        }
        else if (tipo.equals("float")) {
            cargarFloat("$f12", expr);
            out.println("    li $v0, 2");
        }
        else {
            cargar("$a0", expr);
            out.println("    li $v0, 1");
        }

        out.println("    syscall");

        // Salto de línea
        out.println("    la $a0, nl");
        out.println("    li $v0, 4");
        out.println("    syscall");
    }

    private void generarGet(Nodo n) {

        String nombre = n.hijos.get(0).lexema;
        String tipo = tabla.lookup(nombre).tipo;

        if (!tipo.equals("float")) {
            out.println("    li $v0, 5");
            out.println("    syscall");
            out.println("    sw $v0, " + direccion(nombre));
        } else {
            out.println("    li $v0, 6");
            out.println("    syscall");
            out.println("    swc1 $f0, " + direccion(nombre));
        }
    }

    // ____________________________ RETURN ____________________________ 

    private void generarReturn(Nodo n) {

        if (!n.hijos.get(0).getTipo().equals("float")) {
            cargar("$v0", n.hijos.get(0));
        } else {
            cargar("$f0", n.hijos.get(0));
        }

        out.println("    j " + currentFunctionEnd);
    }

    // ____________________________ BREAK ____________________________ 

    private void generarBreak() {
        if (!breakLabels.isEmpty()) {
            out.println("    j " + breakLabels.peek());
        }
    }

    // ____________________________ IF / ELSE ____________________________ 

    private void generarDecide(Nodo n) {

        String Lend = newLabel("decide_end");

        for (int i = 0; i < n.hijos.size(); i++) {

            Nodo hijo = n.hijos.get(i);

            // ELSE
            if (hijo.lexema.equals("ELSE")) {
                generarNodo(hijo.hijos.get(1));
                break;
            }

            String Lnext = newLabel("cond_next");

            Nodo expr = hijo.hijos.get(0);
            Nodo bloque = hijo.hijos.get(2);

            cargar("$t0", expr);
            out.println("    beq $t0, $zero, " + Lnext);

            generarNodo(bloque);
            out.println("    j " + Lend);

            out.println(Lnext + ":");
        }

        out.println(Lend + ":");
    }

    // ____________________________ LOOPS ____________________________ 

    private void generarLoop(Nodo n) {

        String Lstart = newLabel("loop_start");
        String Lend   = newLabel("loop_end");

        breakLabels.push(Lend);

        out.println(Lstart + ":");
        cargar("$t0", n.hijos.get(0));
        out.println("    beq $t0, $zero, " + Lend);

        generarNodo(n.hijos.get(1));
        out.println("    j " + Lstart);

        out.println(Lend + ":");
        breakLabels.pop();
    }

    private void generarFor(Nodo n) {

        String Lstart = newLabel("for_start");
        String Lend   = newLabel("for_end");

        breakLabels.push(Lend);

        generarAsignacion(n.hijos.get(0));

        out.println(Lstart + ":");
        cargar("$t0", n.hijos.get(1));
        out.println("    beq $t0, $zero, " + Lend);

        generarNodo(n.hijos.get(3));
        generarAsignacion(n.hijos.get(2));

        out.println("    j " + Lstart);
        out.println(Lend + ":");

        breakLabels.pop();
    }

    // ____________________________ ARREGLOS ____________________________ 

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
        out.println("    lw $t0, 0($t4)");
    }

    private void guardarElementoArreglo(String nombre, Nodo filaExpr, Nodo colExpr, int columnas) {

        out.println("    move $t5, $t0");

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

    // ____________________________ COMPARACIONES FLOAT ____________________________ 

    private void generarResultadoFloatCmp() {

        String Ltrue = newLabel("ftrue");
        String Lend  = newLabel("fend");

        out.println("    li $t2, 0");
        out.println("    bc1t " + Ltrue);
        out.println("    j " + Lend);

        out.println(Ltrue + ":");
        out.println("    li $t2, 1");

        out.println(Lend + ":");
    }

    private void generarResultadoFloatCmpInvertido() {

        String Ltrue = newLabel("ftrue");
        String Lend  = newLabel("fend");

        out.println("    li $t2, 1");
        out.println("    bc1t " + Ltrue);
        out.println("    li $t2, 0");

        out.println(Ltrue + ":");
        out.println(Lend + ":");
    }

    // ____________________________ FLOAT ____________________________ 

    private void cargarFloat(String freg, Nodo n) {

        // Literal
        if (n.hijos.isEmpty()) {
            out.println("    l.s " + freg + ", " + floatLabels.get(n.lexema));
            return;
        }

        // Variable
        out.println("    l.s " + freg + ", " + direccion(n.lexema));
    }

    // ____________________________ REGISTRO DE LITERALES ____________________________ 

    private void registrarStrings(Nodo n) {
        if (n == null) return;

        if ("string".equals(n.getTipo()) && n.hijos.isEmpty()) {
            registrarString(n.lexema);
        }

        for (Nodo h : n.hijos) registrarStrings(h);
    }

    private void registrarString(String valor) {
        if (!stringLabels.containsKey(valor)) {
            String label = "str_" + stringLabels.size();
            stringLabels.put(valor, label);
            out.println(label + ": .asciiz \"" + valor + "\"");
        }
    }

    private void registrarFloats(Nodo n) {
        if (n == null) return;

        if ("float".equals(n.getTipo()) && n.hijos.isEmpty()) {
            registrarFloat(n.lexema);
        }

        for (Nodo h : n.hijos) registrarFloats(h);
    }

    private void registrarFloat(String valor) {
        if (!floatLabels.containsKey(valor)) {
            String label = "flt_" + floatLabels.size();
            floatLabels.put(valor, label);
            out.println(label + ": .float " + valor);
        }
    }
}
