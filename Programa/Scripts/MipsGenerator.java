import java.io.*;
import java.util.*;

public class MipsGenerator {

    // ____________________________ atributos ____________________________ 

    private final Nodo raiz;
    private final TablaSimbolos tabla;
    private final PrintWriter out;
    private String currentFunctionEnd;
    private int labelCounter = 0;
    private int offsetLocalActual;
    private boolean hasNavidad = false;
    private final Stack<String> breakLabels = new Stack<>();
    private final Map<String, String> stringLabels = new HashMap<>();
    private final Map<String, String> floatLabels  = new HashMap<>();
    private Map<String, Integer> offsetLocales = new HashMap<>();
    private Map<String, Integer> offsetParametros = new HashMap<>();

    // ____________________________ constructor ____________________________ 

    public MipsGenerator(Nodo raiz, TablaSimbolos tabla, String outputFile) throws IOException {
        this.raiz  = raiz;
        this.tabla = tabla;
        this.out   = new PrintWriter(new FileWriter(outputFile));
    }

    // ____________________________ metodos generales ____________________________ 

    private String newLabel(String base) {
        return base + "_" + (labelCounter++);
    }

    public void generar() {
        try (out) {
            System.out.println("inicio generacion MIPS");
            writeHeader();
            generarNodo(raiz);
            
            if (!hasNavidad) {
                throw new RuntimeException(
                        "Error semántico: el programa no contiene el bloque obligatorio COAL NAVIDAD()"
                );
            }
        }
    }

    // ____________________________ helpers stack ____________________________

    private void pushT2() {
        out.println("    addiu $sp, $sp, -4");
        out.println("    sw $t2, 0($sp)");
    }

    private void popTo(String reg) {
        out.println("    lw " + reg + ", 0($sp)");
        out.println("    addiu $sp, $sp, 4");
    }

    // ____________________________ header ____________________________ 

    private void writeHeader() {
        out.println(".data");
        out.println("nl: .asciiz \"\\n\"");

        for (TablaSimbolos.SymbolInfo s : tabla.getGlobalSymbols()) {
            if (!"global".equals(s.categoria)) continue;
            if (s.esArreglo) {
                int total = Math.max(1, s.filas) * Math.max(1, s.columnasArreglo);
                out.println(s.nombre + ": .space " + (total * 4));
                continue;}

            switch (s.tipo) {
                case "int", "boolean", "char" -> out.println(s.nombre + ": .word 0");
                case "float" -> out.println(s.nombre + ": .float 0.0");
                case "string" -> out.println(s.nombre + ": .asciiz \"\"");
                default -> out.println(s.nombre + ": .word 0");}
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

    // ____________________________ generacion nodos ____________________________ 

    private void generarNodo(Nodo n) {
    
        System.out.println("vistando nodo: " + (n == null ? "null" : n.lexema));
        if (n == null) return;

        switch (n.lexema) {
            case "FUNCION" -> generarFuncion(n);
            case "llamada" -> generarLlamadaFuncion(n);
            case "PROGRAMA", "GLOBALES", "FUNCIONES", "BLOQUE", "SENTENCIAS" -> {
                if (n.hijos != null) {
                    for (Nodo h : n.hijos) generarNodo(h);
                }
            }
            case "MAIN" -> {
                System.out.println("entrando a main");
                hasNavidad = true;
                String oldEnd = currentFunctionEnd;
                currentFunctionEnd = "NAVIDAD_END";

                out.println();
                out.println("NAVIDAD:");
                out.println("    # Prologo de NAVIDAD");
                out.println("    addiu $sp, $sp, -8");
                out.println("    sw $ra, 4($sp)");
                out.println("    sw $fp, 0($sp)");
                out.println("    move $fp, $sp");
                offsetLocales.clear();
                offsetParametros.clear();
                offsetLocalActual = -4;

                if (n.hijos != null) {
                    for (Nodo h : n.hijos) generarNodo(h);}

                out.println(currentFunctionEnd + ":");
                out.println("    move $sp, $fp");
                out.println("    lw $fp, 0($sp)");
                out.println("    lw $ra, 4($sp)");
                out.println("    addiu $sp, $sp, 8");
                out.println("    jr $ra");
                currentFunctionEnd = oldEnd;
            }
            case "ASIGNACION" -> generarAsignacion(n);
            case "SHOW" -> generarShow(n);
            case "GET" -> generarGet(n);
            case "RETURN" -> generarReturn(n);
            case "BREAK" -> generarBreak();
            case "DECIDE" -> generarDecide(n);
            case "LOOP" -> generarLoop(n);
            case "FOR" -> generarFor(n);
            case "DECL_LOCAL" -> {
                String nombre = n.hijos.get(1).lexema;
                offsetLocalActual -= 4;
                offsetLocales.put(nombre, offsetLocalActual);
                out.println("    # reserva local " + nombre);
                out.println("    addiu $sp, $sp, -4");
                out.println("    sw $zero, " + direccion(nombre));
            }
            case "+", "-", "*", "/", "//", "%", "^",
                 ">", "<", ">=", "<=", "==", "!=",
                 "@", "~", "Σ", "++", "--" -> generarExpr(n);
            default -> {
                if (n.hijos != null) {
                    for (Nodo h : n.hijos) generarNodo(h);}}
        }
    }

    // ____________________________ funciones ____________________________ 

    private void generarFuncion(Nodo n) {
        String nombre = n.hijos.get(1).lexema;
        Nodo params = n.hijos.get(2);
        Nodo bloque = n.hijos.get(3);
        String oldEnd = currentFunctionEnd;
        currentFunctionEnd = newLabel(nombre + "_end");

        out.println();
        out.println(nombre + ":");
        out.println("    # Prologo de funcion");
        out.println("    addiu $sp, $sp, -8");
        out.println("    sw $ra, 4($sp)");
        out.println("    sw $fp, 0($sp)");
        out.println("    move $fp, $sp");
        // frame 
        offsetLocales.clear();
        offsetParametros.clear();
        offsetLocalActual = -4;
        // recolectar ids de params
        List<String> ids = new ArrayList<>();
        recolectarIdsParams(params, ids);
        int offsetParam = 8; // 8($fp) = primer param, 12($fp) = segundo...
        for (String id : ids) {
            offsetParametros.put(id, offsetParam);
            offsetParam += 4;
        }
        //  Generar bloque 
        generarNodo(bloque);
        out.println(currentFunctionEnd + ":");
        out.println("    move $sp, $fp");
        out.println("    lw $fp, 0($sp)");
        out.println("    lw $ra, 4($sp)");
        out.println("    addiu $sp, $sp, 8");
        out.println("    jr $ra");

        currentFunctionEnd = oldEnd;
    }

    private void recolectarIdsParams(Nodo n, List<String> outIds) {
        if (n == null || n.hijos == null) return;
        if ("itemParams".equals(n.lexema)) {
            if (n.hijos.size() == 2) {
                outIds.add(n.hijos.get(1).lexema);
                return;}
            if (n.hijos.size() >= 4) {
                recolectarIdsParams(n.hijos.get(0), outIds);
                outIds.add(n.hijos.get(n.hijos.size() - 1).lexema);
                return;}
        }   for (Nodo h : n.hijos) recolectarIdsParams(h, outIds);
    }

    private void generarLlamadaFuncion(Nodo n) {
        String nombre = n.hijos.get(0).lexema;
        List<Nodo> params = new ArrayList<>();

        if (n.hijos.size() > 1) {
            Nodo args = n.hijos.get(1);
            if (args != null && args.hijos != null) {
                for (Nodo h : args.hijos) {
                    if (!",".equals(h.lexema)) params.add(h);}}
        }
        int pushed = 0;
        for (int i = params.size() - 1; i >= 0; i--) {
            Nodo p = params.get(i);
            if ("float".equals(p.getTipo())) {
                cargarFloat("$f0", p);
                out.println("    addiu $sp, $sp, -4");
                out.println("    swc1 $f0, 0($sp)");
            } else {
                generarExpr(p);
                out.println("    addiu $sp, $sp, -4");
                out.println("    sw $t2, 0($sp)");
            } pushed++;
        }
        out.println("    jal " + nombre);
        if (pushed > 0) {
            out.println("    addiu $sp, $sp, " + (pushed * 4));
        }
        TablaSimbolos.SymbolInfo f = tabla.lookup(nombre);
        if (f != null && !"float".equals(f.tipo)) {
            out.println("    move $t2, $v0");
        }
    }

    private String direccion(String nombre) {
        // locales
        if (offsetLocales.containsKey(nombre)) {
            return offsetLocales.get(nombre) + "($fp)";}
        // params
        if (offsetParametros.containsKey(nombre)) {
            return offsetParametros.get(nombre) + "($fp)";}
        // global por tabla
        TablaSimbolos.SymbolInfo s = tabla.lookup(nombre);
        if (s != null && "global".equals(s.categoria)) {
            return nombre;}
        return nombre;
    }

    // ____________________________ expresiones ____________________________ 

private void generarExpr(Nodo n) {

    if (n == null) return;
    if ("llamada".equals(n.lexema)) {
        generarLlamadaFuncion(n);
        return;}
    if (n.hijos == null || n.hijos.isEmpty()) {
        if ("float".equals(n.getTipo())) cargarFloat("$f0", n);
        else cargar("$t2", n);
        return;}
    if ("Σ".equals(n.lexema)) {
        Nodo op = n.hijos.get(0);
        generarExpr(op);
        out.println("    seq $t2, $t2, $zero");
        return;}

    Nodo izq = n.hijos.get(0);
    Nodo der = n.hijos.size() > 1 ? n.hijos.get(1) : null;

    if ("float".equals(n.getTipo())) {
        cargarFloat("$f1", izq);
        if (der != null) cargarFloat("$f2", der);
        switch (n.lexema) {
            case "+" -> out.println("    add.s $f0, $f1, $f2");
            case "-" -> out.println("    sub.s $f0, $f1, $f2");
            case "*" -> out.println("    mul.s $f0, $f1, $f2");
            case "/" -> out.println("    div.s $f0, $f1, $f2");
            case "<" -> { out.println("    c.lt.s $f1, $f2"); generarResultadoFloatCmp(); }
            case ">" -> { out.println("    c.le.s $f2, $f1"); generarResultadoFloatCmp(); }
            case "<=" -> { out.println("    c.le.s $f1, $f2"); generarResultadoFloatCmp(); }
            case ">=" -> { out.println("    c.lt.s $f2, $f1"); generarResultadoFloatCmp(); }
            case "==" -> { out.println("    c.eq.s $f1, $f2"); generarResultadoFloatCmp(); }
            case "!=" -> { out.println("    c.eq.s $f1, $f2"); generarResultadoFloatCmpInvertido(); }}
            return;}

        generarExpr(izq);
        pushT2(); 
        
        if (der != null) {
            generarExpr(der);
            out.println("    move $t9, $t2");
        } else {
            out.println("    move $t9, $zero");
        }

        popTo("$t8");

        switch (n.lexema) {
            case "+" -> out.println("    add $t2, $t8, $t9");
            case "-" -> out.println("    sub $t2, $t8, $t9");
            case "*" -> out.println("    mul $t2, $t8, $t9");
            case "/", "//" -> {
                out.println("    div $t8, $t9");
                out.println("    mflo $t2");
            }
            case "%" -> {
                out.println("    div $t8, $t9");
                out.println("    mfhi $t2");
            }
            case "^" -> {
                out.println("    move $t0, $t8");
                out.println("    move $t1, $t9");
                generarPotencia(); // deja en $t2
            }
            case ">"  -> out.println("    sgt $t2, $t8, $t9");
            case "<"  -> out.println("    slt $t2, $t8, $t9");
            case ">=" -> out.println("    sge $t2, $t8, $t9");
            case "<=" -> out.println("    sle $t2, $t8, $t9");
            case "==" -> out.println("    seq $t2, $t8, $t9");
            case "!=" -> out.println("    sne $t2, $t8, $t9");
            case "@" -> out.println("    and $t2, $t8, $t9");
            case "~" -> out.println("    or  $t2, $t8, $t9");
            case "++" -> {
                Nodo var = n.hijos.get(0);
                cargar("$t0", var);
                out.println("    addi $t0, $t0, 1");
                out.println("    sw $t0, " + direccion(var.lexema));
                out.println("    move $t2, $t0");
            }
            case "--" -> {
                Nodo var = n.hijos.get(0);
                cargar("$t0", var);
                out.println("    addi $t0, $t0, -1");
                out.println("    sw $t0, " + direccion(var.lexema));
                out.println("    move $t2, $t0");
            }
        }
    }

    // ____________________________ potencia ____________________________ 

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

    // ____________________________ carga de valores ____________________________ 

    private void cargar(String reg, Nodo n) {

        if (n == null) return;
        if ("float".equals(n.getTipo())) {
            if (reg != null && reg.startsWith("$f")) {
                cargarFloat(reg, n);
            } else {
                cargarFloat("$f0", n);
                if (reg != null) out.println("    mfc1 " + reg + ", $f0");
            } return;
        }
        if (n.hijos == null || n.hijos.isEmpty()) {
            if ("true".equals(n.lexema)) {
                out.println("    li " + reg + ", 1");
                return;}
            if ("false".equals(n.lexema)) {
                out.println("    li " + reg + ", 0");
                return;}
            if (n.lexema != null && n.lexema.matches("-?\\d+")) {
                out.println("    li " + reg + ", " + n.lexema);
                return;}
            if (n.lexema != null && n.lexema.length() >= 3 && n.lexema.startsWith("'") && n.lexema.endsWith("'")) {
                int v = (int) n.lexema.charAt(1);
                out.println("    li " + reg + ", " + v);
                return;}
            out.println("    lw " + reg + ", " + direccion(n.lexema));
            return;
        }
        if (n.lexema != null && n.lexema.contains("Filas")) {
            String nombre = n.hijos.get(0).lexema;
            Nodo fila = n.hijos.get(1);
            Nodo col  = n.hijos.get(2);
            int columnas = tabla.lookup(nombre).columnasArreglo;
            cargarElementoArreglo(nombre, fila, col, columnas);
            out.println("    move " + reg + ", $t0");
            return;
        }
            generarExpr(n);
            out.println("    move " + reg + ", $t2");
    }

    // ____________________________ asignaciones ____________________________ 
    
    private void generarAsignacion(Nodo n) {

        Nodo lhs = n.hijos.get(0);
        Nodo rhs = n.hijos.get(1);
        generarExpr(rhs);
        if (lhs.lexema.contains("Filas")) {
            String nombre = lhs.hijos.get(0).lexema;
            Nodo fila = lhs.hijos.get(1);
            Nodo col  = lhs.hijos.get(2);
            int columnas = tabla.lookup(nombre).columnasArreglo;
            guardarElementoArreglo(nombre, fila, col, columnas);
        } else {
            out.println("    sw $t2, " + direccion(lhs.lexema));}
    }

    // ____________________________ show y get ____________________________ 

    private void generarShow(Nodo n) {

        if (n == null || n.hijos == null || n.hijos.isEmpty()) return;
        Nodo expr = n.hijos.get(0);
        String tipo = expr.getTipo();
        boolean esStringLiteral = expr.lexema != null && expr.lexema.length() >= 2
                && expr.lexema.startsWith("\"") && expr.lexema.endsWith("\"");
        if (esStringLiteral || "string".equals(tipo)) {
            out.println("    la $a0, " + stringLabels.get(expr.lexema));
            out.println("    li $v0, 4");
            out.println("    syscall");
        } else if ("float".equals(tipo)) {
            cargarFloat("$f12", expr);
            out.println("    li $v0, 2");
            out.println("    syscall");
        } else {
            cargar("$a0", expr);
            out.println("    li $v0, 1");
            out.println("    syscall");
        }
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

    // ____________________________ return ____________________________ 

    private void generarReturn(Nodo n) {
        if (!"float".equals(n.hijos.get(0).getTipo())) {
            cargar("$v0", n.hijos.get(0));
        } else {
            cargarFloat("$f0", n.hijos.get(0));}
        if (currentFunctionEnd != null) {
            out.println("    j " + currentFunctionEnd);
        } else {
            out.println("    jr $ra");}
    }

    // ____________________________ break ____________________________ 

    private void generarBreak() {
        if (!breakLabels.isEmpty()) {
            out.println("    j " + breakLabels.peek());
        }
    }

    // ____________________________ if y else ____________________________ 

    private void generarDecide(Nodo n) {

        String Lend = newLabel("decide_end");
        Nodo condicion = n.hijos.get(0);
        Nodo elseNodo = n.hijos.size() > 1 ? n.hijos.get(1) : null;
        String Lelse = (elseNodo != null) ? newLabel("decide_else") : Lend;
        Nodo exprCond = condicion.hijos.get(0);
        Nodo bloqueThen = condicion.hijos.get(2);

        generarExpr(exprCond);
        out.println("    beq $t2, $zero, " + Lelse);
        generarNodo(bloqueThen);
        out.println("    j " + Lend);

        if (elseNodo != null) {
            out.println(Lelse + ":");
            Nodo bloqueElse = elseNodo.hijos.get(1);
            generarNodo(bloqueElse);}
        out.println(Lend + ":");
    }

    // ____________________________ loops ____________________________ 

    private void generarLoop(Nodo n) {
        String Lstart = newLabel("loop_start");
        String Lend = newLabel("loop_end");
        breakLabels.push(Lend);
        out.println(Lstart + ":");
        Nodo condicion = n.hijos.get(0);
        Nodo bloque = n.hijos.get(2);
        generarExpr(condicion);
        out.println("    beq $t2, $zero, " + Lend);
        generarNodo(bloque);
        out.println("    j " + Lstart);
        out.println(Lend + ":");
        breakLabels.pop();
    }

    // ____________________________ for ____________________________

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

    // ____________________________ array ____________________________ 

    private void cargarElementoArreglo(String nombre, Nodo fila, Nodo col, int columnas) {
        generarExpr(fila);
        out.println("    move $t3, $t2");
        generarExpr(col);
        out.println("    move $t4, $t2");
        out.println("    li $t5, " + columnas);
        out.println("    mul $t3, $t3, $t5");
        out.println("    add $t3, $t3, $t4");
        out.println("    sll $t3, $t3, 2");
        out.println("    la $t6, " + nombre);
        out.println("    add $t6, $t6, $t3");
        out.println("    lw $t0, 0($t6)");
    }

    private void guardarElementoArreglo(String nombre, Nodo fila, Nodo col, int columnas) {
        out.println("    move $t7, $t2");
        generarExpr(fila);
        out.println("    move $t3, $t2");
        generarExpr(col);
        out.println("    move $t4, $t2");
        out.println("    li $t5, " + columnas);
        out.println("    mul $t3, $t3, $t5");
        out.println("    add $t3, $t3, $t4");
        out.println("    sll $t3, $t3, 2");
        out.println("    la $t6, " + nombre);
        out.println("    add $t6, $t6, $t3");
        out.println("    sw $t7, 0($t6)");
    }

    // ____________________________ FLOAT ____________________________ 

    private void registrarFloats(Nodo n) {

        if (n == null) return;
        boolean hoja = (n.hijos == null || n.hijos.isEmpty());
        if ("float".equals(n.getTipo()) && hoja) {
            if (n.lexema != null && n.lexema.matches("-?\\d+(\\.\\d+)?")) {
                if (!floatLabels.containsKey(n.lexema)) {
                    String label = newLabel("flt");
                    floatLabels.put(n.lexema, label);
                    out.println(label + ": .float " + n.lexema);}
            }
        }
        if (n.hijos != null) {
            for (Nodo h : n.hijos) registrarFloats(h);}
    }

    private void cargarFloat(String freg, Nodo n) {
        
        if (n == null) return;
        boolean hoja = (n.hijos == null || n.hijos.isEmpty());
        if (hoja) {
            // literal float
            if (n.lexema != null && n.lexema.matches("-?\\d+(\\.\\d+)?")) {
                String label = floatLabels.get(n.lexema);
                if (label == null) {
                    label = newLabel("flt");
                    floatLabels.put(n.lexema, label);
                    out.println(label + ": .float " + n.lexema);
                }
                out.println("    lwc1 " + freg + ", " + label);
                return;}

            out.println("    lwc1 " + freg + ", " + direccion(n.lexema));
            return;}

        generarExpr(n);
        out.println("    mov.s " + freg + ", $f0");}

    private void generarResultadoFloatCmp() {
        String Ltrue = newLabel("fcmp_true");
        String Lend  = newLabel("fcmp_end");
        out.println("    bc1t " + Ltrue);
        out.println("    li $t2, 0");
        out.println("    j " + Lend);
        out.println(Ltrue + ":");
        out.println("    li $t2, 1");
        out.println(Lend + ":");
    }

    private void generarResultadoFloatCmpInvertido() {
        String Ltrue = newLabel("fcmp_true");
        String Lend  = newLabel("fcmp_end");
        out.println("    bc1f " + Ltrue);
        out.println("    li $t2, 0");
        out.println("    j " + Lend);
        out.println(Ltrue + ":");
        out.println("    li $t2, 1");
        out.println(Lend + ":");
    }

        // ____________________________ STRINGS ____________________________

    private void registrarStrings(Nodo n) {
        if (n == null) return;
        if (n.lexema != null && n.lexema.startsWith("\"") && n.lexema.endsWith("\"")) {
            if (!stringLabels.containsKey(n.lexema)) {
                String label = newLabel("str");
                stringLabels.put(n.lexema, label);
                out.println(label + ": .asciiz " + n.lexema);}
        }
        if (n.hijos != null) {
            for (Nodo h : n.hijos) registrarStrings(h);
        }
    }

}