import java.io.*;
import java.util.*;

public class MipsGenerator {

    // ____________________________ atributos ____________________________ 

    private final Nodo raiz; // Nodo raíz del árbol sintáctico, para "empezar" a recorrerlo
    private final TablaSimbolos tabla; 
    private final PrintWriter out;
    private String currentFunctionEnd;
    private int labelCounter = 0; // marca el final de una funcion, es mas que nada por control
    private int offsetLocalActual; //controla el desplz de las var locales en stack
    private boolean hasNavidad = false;
    private final Stack<String> breakLabels = new Stack<>();
    private final Map<String, String> stringLabels = new HashMap<>(); //asocian
    private final Map<String, String> floatLabels  = new HashMap<>();
    @SuppressWarnings("FieldMayBeFinal") // solo para quita advertencia que saltaba
    private Map<String, Integer> offsetLocales = new HashMap<>(); 
    @SuppressWarnings("FieldMayBeFinal")
    private Map<String, Integer> offsetParametros = new HashMap<>();

    // ____________________________ constructor ____________________________ 

    public MipsGenerator(Nodo raiz, TablaSimbolos tabla, String outputFile) throws IOException { //como el nombre lo dice, genera el codigo MIPS
        this.raiz  = raiz;
        this.tabla = tabla;
        this.out   = new PrintWriter(new FileWriter(outputFile)); // recibe todo y genera archivo de salida MIPS
    }

    // ____________________________ metodos generales ____________________________ 

    private String newLabel(String base) {
        return base + "_" + (labelCounter++);
    } // se utiliza para crear saltos y marcas en el código MIPS

    public void generar() { 
        try (out) {
            System.out.println("inicio generacion MIPS");
            writeHeader(); // escribe la sección .data y .text 
            generarNodo(raiz); // recorre el árbol y genera instrucciones
            
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
    } // funciones auxiliares para guardar y recuperar valores en el stack (ambas son para eso, son el tipico pop y push)

    // ____________________________ header ____________________________ 

    private void writeHeader() {
        out.println(".data");
        out.println("nl: .asciiz \"\\n\"");

        for (TablaSimbolos.SymbolInfo s : tabla.getGlobalSymbols()) {
            if (!"global".equals(s.categoria)) continue;
            if (s.esArreglo) {
                int total = Math.max(1, s.filas) * Math.max(1, s.columnasArreglo);
                out.println(s.nombre + ": .space " + (total * 4));
                continue;} //recorre todos los simbolos globales de la tabla

            switch (s.tipo) {
                case "int", "boolean", "char" -> out.println(s.nombre + ": .word 0");
                case "float" -> out.println(s.nombre + ": .float 0.0");
                case "string" -> out.println(s.nombre + ": .asciiz \"\"");
                default -> out.println(s.nombre + ": .word 0");}
        } //selecciona la "etiqueta" del dato segun el tipo del simbolo
        registrarStrings(raiz);
        registrarFloats(raiz);

        out.println();
        out.println(".text");
        out.println(".globl main");
        out.println("main:");
        out.println("    jal NAVIDAD");
        out.println("    li $v0, 10");
        out.println("    syscall");
    } // escribe la cabecera del programa MIPS, incluyendo la sección .data y .text (lo que ya se dije en el generador)

    // ____________________________ generacion nodos ____________________________ 

    private void generarNodo(Nodo n) {
    
        //System.out.println("vistando nodo: " + (n == null ? "null" : n.lexema)); //Esto solo era de guia para ver en que nodo esta entrando
        if (n == null) return; //recorre el árbol sintáctico y genera instrucciones MIPS según el tipo de nodo

        switch (n.lexema) {
            case "FUNCION" -> generarFuncion(n);
            case "llamada" -> generarLlamadaFuncion(n);
            case "PROGRAMA", "GLOBALES", "FUNCIONES", "BLOQUE", "SENTENCIAS" -> {
                if (n.hijos != null) {
                    for (Nodo h : n.hijos) generarNodo(h);
                }
            } //para navidad
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
            } // casos específicos para distintos tipos de sentencias
            case "ASIGNACION" -> generarAsignacion(n);
            case "SHOW" -> generarShow(n);
            case "GET" -> generarGet(n);
            case "RETURN" -> generarReturn(n);
            case "BREAK" -> generarBreak();
            case "DECIDE" -> generarDecide(n);
            case "LOOP" -> generarLoop(n);
            case "FOR" -> generarFor(n);
            case "DECL_LOCAL" -> { // declaración de variable local, reserva espacio en el stack
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
            default -> { //en caso de que no tenga hijos
                if (n.hijos != null) {
                    for (Nodo h : n.hijos) generarNodo(h);}}
        }
    }

    // ____________________________ funciones ____________________________ 

    private void generarFuncion(Nodo n) {
        String nombre = n.hijos.get(1).lexema; // nombre de la función
        Nodo params = n.hijos.get(2); // parámetros de la función
        Nodo bloque = n.hijos.get(3); // cuerpo de la función
        String oldEnd = currentFunctionEnd;
        currentFunctionEnd = newLabel(nombre + "_end");

        out.println();
        out.println(nombre + ":");
        out.println("    # Prologo de funcion");
        out.println("    addiu $sp, $sp, -8");
        out.println("    sw $ra, 4($sp)");
        out.println("    sw $fp, 0($sp)");
        out.println("    move $fp, $sp");
        // frame de la funcion (inicializacion)
        offsetLocales.clear();
        offsetParametros.clear();
        offsetLocalActual = -4;
        List<String> ids = new ArrayList<>(); // recolecta los identificadores de los parámetros
        recolectarIdsParams(params, ids);
        int offsetParam = 8; // primer parámetro en 8($fp), segundo en 12($fp), y asi sucesivamente 
        for (String id : ids) {
            offsetParametros.put(id, offsetParam);
            offsetParam += 4;
        }
        generarNodo(bloque); //  Genera bloque 
        out.println(currentFunctionEnd + ":");
        out.println("    move $sp, $fp");
        out.println("    lw $fp, 0($sp)");
        out.println("    lw $ra, 4($sp)");
        out.println("    addiu $sp, $sp, 8");
        out.println("    jr $ra");

        currentFunctionEnd = oldEnd;
    }

    private void recolectarIdsParams(Nodo n, List<String> outIds) { // recolecta los identificadores de parámetros en un nodo de tipo itemParams
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
    } // se usa para construir la lista de parámetros de una función y asignarles offsets en el stack

    private void generarLlamadaFuncion(Nodo n) {
        String nombre = n.hijos.get(0).lexema;
        List<Nodo> params = new ArrayList<>(); 
        // evalúa los parámetros, los coloca en el stack, invoca la función y recupera el resultado
        if (n.hijos.size() > 1) {
            Nodo args = n.hijos.get(1);
            if (args != null && args.hijos != null) {
                for (Nodo h : args.hijos) {
                    if (!",".equals(h.lexema)) params.add(h);}}
        }
        int pushed = 0;
        for (int i = params.size() - 1; i >= 0; i--) { //recorre los parametros de manera inversa y los guarda em stack
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
        out.println("    jal " + nombre); // limpia el stack después de la llamada, importante para que mantenga la coherencia 
        if (pushed > 0) {
            out.println("    addiu $sp, $sp, " + (pushed * 4));
        }
        TablaSimbolos.SymbolInfo f = tabla.lookup(nombre); // mueve el resultado a $t2 si la función devuelve un entero o booleano
        if (f != null && !"float".equals(f.tipo)) {
            out.println("    move $t2, $v0");
        }
    }

    private String direccion(String nombre) {
        if (offsetLocales.containsKey(nombre)) {
            return offsetLocales.get(nombre) + "($fp)";} //revisa var local
        if (offsetParametros.containsKey(nombre)) {
            return offsetParametros.get(nombre) + "($fp)";} //revisa parametros de la func
        TablaSimbolos.SymbolInfo s = tabla.lookup(nombre);
        if (s != null && "global".equals(s.categoria)) {
            return nombre;} // busca si es var global (apoyandose en la tabla de simbolos)
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
    if ("Σ".equals(n.lexema)) { // compara con cero
        Nodo op = n.hijos.get(0);
        generarExpr(op);
        out.println("    seq $t2, $t2, $zero");
        return;}

    Nodo izq = n.hijos.get(0); // hijo izq
    Nodo der = n.hijos.size() > 1 ? n.hijos.get(1) : null; //hijo der si es que existe 

    if ("float".equals(n.getTipo())) {
        cargarFloat("$f1", izq);
        if (der != null) cargarFloat("$f2", der);
        switch (n.lexema) { // selecciona operación de punto flotante
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

        generarExpr(izq); // evalúa hijo izquierdo
        pushT2(); // guarda resultado en stack
        
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
            case "++" -> { //incr.
                Nodo var = n.hijos.get(0);
                cargar("$t0", var);
                out.println("    addi $t0, $t0, 1");
                out.println("    sw $t0, " + direccion(var.lexema));
                out.println("    move $t2, $t0");
            }
            case "--" -> { // decremento de variable
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
        String Lloop = newLabel("pow_loop"); //etiqueta para inicio de ciclo
        String Lend  = newLabel("pow_end"); //fin de ciclo 
        out.println("    li $t2, 1"); //inicia en 1
        out.println(Lloop + ":");
        out.println("    beq $t1, $zero, " + Lend); //evalua exponente, si es 0 termina
        out.println("    mul $t2, $t2, $t0"); //multiplica por base
        out.println("    addi $t1, $t1, -1"); //repite ciclo
        out.println("    j " + Lloop);
        out.println(Lend + ":");
    }

    // ____________________________ carga de valores ____________________________ 

private void cargar(String reg, Nodo n) {
    if (n == null) return; // evita procesar nodos nulos
    if ("float".equals(n.getTipo())) { // si es float, carga en registro de punto flotante
        if (reg != null && reg.startsWith("$f")) {
            cargarFloat(reg, n);
        } else {
            cargarFloat("$f0", n);
            if (reg != null) out.println("    mfc1 " + reg + ", $f0"); // mueve float a registro entero
        }
        return;
    }
    if (n.hijos == null || n.hijos.isEmpty()) { // si es hoja, maneja literales o variables simples
        if ("true".equals(n.lexema)) {
            out.println("    li " + reg + ", 1"); // literal booleano true
            return;
        }
        if ("false".equals(n.lexema)) {
            out.println("    li " + reg + ", 0"); // literal booleano false
            return;
        }
        if (n.lexema != null && n.lexema.matches("-?\\d+")) {
            out.println("    li " + reg + ", " + n.lexema); // literal entero
            return;
        }
        if (n.lexema != null && n.lexema.length() >= 3 && n.lexema.startsWith("'") && n.lexema.endsWith("'")) {
            int v = (int) n.lexema.charAt(1);
            out.println("    li " + reg + ", " + v); // literal char
            return;
        }
        out.println("    lw " + reg + ", " + direccion(n.lexema)); // carga variable desde memoria
        return;
    }
    if (n.lexema != null && n.lexema.contains("Filas")) { // acceso a arreglo
        String nombre = n.hijos.get(0).lexema;
        Nodo fila = n.hijos.get(1);
        Nodo col  = n.hijos.get(2);
        int columnas = tabla.lookup(nombre).columnasArreglo;
        cargarElementoArreglo(nombre, fila, col, columnas);
        out.println("    move " + reg + ", $t0"); // mueve valor del arreglo al registro
        return;
    }
    generarExpr(n); // si es expresión compuesta, evalúa
    out.println("    move " + reg + ", $t2"); // mueve resultado al registro indicado
}

    // ____________________________ asignaciones ____________________________ 
    
private void generarAsignacion(Nodo n) {
    Nodo lhs = n.hijos.get(0); // lado izq de la asignación
    Nodo rhs = n.hijos.get(1); // lado der de la asignación
    generarExpr(rhs); // evalúa la expresión del lado derecho
    if (lhs.lexema.contains("Filas")) { // si es acceso a arreglo
        String nombre = lhs.hijos.get(0).lexema;
        Nodo fila = lhs.hijos.get(1);
        Nodo col  = lhs.hijos.get(2);
        int columnas = tabla.lookup(nombre).columnasArreglo;
        guardarElementoArreglo(nombre, fila, col, columnas); // guarda valor en posición del arreglo
    } else {
        out.println("    sw $t2, " + direccion(lhs.lexema)); // guarda valor en variable normal
    }
}

    // ____________________________ show y get ____________________________ 

    private void generarShow(Nodo n) {

        if (n == null || n.hijos == null || n.hijos.isEmpty()) return; // evita nodos vacíos
        Nodo expr = n.hijos.get(0); // expresión a mostrar
        String tipo = expr.getTipo(); // tipo de dato de la expresión
        boolean esStringLiteral = expr.lexema != null && expr.lexema.length() >= 2
                && expr.lexema.startsWith("\"") && expr.lexema.endsWith("\""); // detecta literal string
        if (esStringLiteral || "string".equals(tipo)) { // imprime string
            out.println("    la $a0, " + stringLabels.get(expr.lexema));
            out.println("    li $v0, 4");
            out.println("    syscall");
        } else if ("float".equals(tipo)) { // para floats (los imprime)
            cargarFloat("$f12", expr);
            out.println("    li $v0, 2");
            out.println("    syscall");
        } else { // imprime entero o booleano
            cargar("$a0", expr);
            out.println("    li $v0, 1");
            out.println("    syscall");
        }
        out.println("    la $a0, nl"); // salto de linea (imprime)
        out.println("    li $v0, 4");
        out.println("    syscall");
    }

    private void generarGet(Nodo n) {
        String nombre = n.hijos.get(0).lexema; // variable destino
        String tipo = tabla.lookup(nombre).tipo; // tipo de la variable
        if (!tipo.equals("float")) { // lectura de entero o booleano
            out.println("    li $v0, 5");
            out.println("    syscall");
            out.println("    sw $v0, " + direccion(nombre));
        } else { // lectura de float
            out.println("    li $v0, 6");
            out.println("    syscall");
            out.println("    swc1 $f0, " + direccion(nombre));
        }
    }

    // ____________________________ return ____________________________ 

    private void generarReturn(Nodo n) {
        if (!"float".equals(n.hijos.get(0).getTipo())) {
            cargar("$v0", n.hijos.get(0)); // guarda valor entero/booleano en $v0
        } else {
            cargarFloat("$f0", n.hijos.get(0)); // guarda valor float en $f0
        }
        if (currentFunctionEnd != null) {
            out.println("    j " + currentFunctionEnd); // salta al epílogo de la función (parte final de una funcion ensamblador)
        } else {
            out.println("    jr $ra"); // retorna directamente si no hay etiqueta de fin
        }
    }

    // ____________________________ break ____________________________ 

    private void generarBreak() {
        if (!breakLabels.isEmpty()) {
            out.println("    j " + breakLabels.peek()); // salta a la etiqueta de salida del ciclo
        }
    }

    // ____________________________ if y else ____________________________ 

    private void generarDecide(Nodo n) {

        String Lend = newLabel("decide_end"); //fin del if
        Nodo condicion = n.hijos.get(0); //nodo con cond
        Nodo elseNodo = n.hijos.size() > 1 ? n.hijos.get(1) : null;
        String Lelse = (elseNodo != null) ? newLabel("decide_else") : Lend;
        Nodo exprCond = condicion.hijos.get(0); //expr condicional
        Nodo bloqueThen = condicion.hijos.get(2); //bloque then

        generarExpr(exprCond); //condicion
        out.println("    beq $t2, $zero, " + Lelse);
        generarNodo(bloqueThen);
        out.println("    j " + Lend);

        if (elseNodo != null) { //else
            out.println(Lelse + ":");
            Nodo bloqueElse = elseNodo.hijos.get(1);
            generarNodo(bloqueElse);}
        out.println(Lend + ":"); //fin
    }

    // ____________________________ loops ____________________________ 

    private void generarLoop(Nodo n) {
        String Lstart = newLabel("loop_start"); //inicio del ciclo
        String Lend = newLabel("loop_end"); //fin
        breakLabels.push(Lend); //guarda etiqeta break
        out.println(Lstart + ":");
        Nodo condicion = n.hijos.get(0);
        Nodo bloque = n.hijos.get(2); //cuerpo del ciclo
        generarExpr(condicion);
        out.println("    beq $t2, $zero, " + Lend);
        generarNodo(bloque);
        out.println("    j " + Lstart);
        out.println(Lend + ":"); //fin ciclo
        breakLabels.pop(); //quita etiqueta break
    }

    // ____________________________ for ____________________________

    private void generarFor(Nodo n) {
        String Lstart = newLabel("for_start"); // inicio for
        String Lend   = newLabel("for_end"); // 
        breakLabels.push(Lend);
        generarAsignacion(n.hijos.get(0));
        out.println(Lstart + ":");
        cargar("$t0", n.hijos.get(1)); //condicion
        out.println("    beq $t0, $zero, " + Lend);
        generarNodo(n.hijos.get(3)); // cuerpo del for
        generarAsignacion(n.hijos.get(2)); //actualizacion
        out.println("    j " + Lstart);
        out.println(Lend + ":"); // fin for
        breakLabels.pop();
    }

    // ____________________________ array ____________________________ 

    private void cargarElementoArreglo(String nombre, Nodo fila, Nodo col, int columnas) {
        generarExpr(fila); // evalúa índice de fila
        out.println("    move $t3, $t2");
        generarExpr(col); // evalúa índice de columna
        out.println("    move $t4, $t2");
        out.println("    li $t5, " + columnas); // número de columnas del arreglo
        out.println("    mul $t3, $t3, $t5"); // fila * columnas
        out.println("    add $t3, $t3, $t4"); // suma columna
        out.println("    sll $t3, $t3, 2"); // multiplica por 4 (tamaño de palabra)
        out.println("    la $t6, " + nombre); // dirección base del arreglo
        out.println("    add $t6, $t6, $t3"); // dirección del elemento
        out.println("    lw $t0, 0($t6)"); // carga valor en $t0
    }

    private void guardarElementoArreglo(String nombre, Nodo fila, Nodo col, int columnas) {
        out.println("    move $t7, $t2"); //guarda valor a asignar
        generarExpr(fila); // evalua indice de fila
        out.println("    move $t3, $t2");
        generarExpr(col); // evalua indice de columna
        out.println("    move $t4, $t2");
        out.println("    li $t5, " + columnas); //numero de columnas del arreglo
        out.println("    mul $t3, $t3, $t5"); //fila x columnas
        out.println("    add $t3, $t3, $t4"); //suma columna
        out.println("    sll $t3, $t3, 2"); //multiplica por 4 (tamaño de palabra)
        out.println("    la $t6, " + nombre); // dirección base del arreglo
        out.println("    add $t6, $t6, $t3"); // dirección del elemento
        out.println("    sw $t7, 0($t6)"); // guarda valor en posición calculada
    }

    // ____________________________ FLOAT ____________________________ 

    private void registrarFloats(Nodo n) {

        if (n == null) return; // evita nodos nulos
        boolean hoja = (n.hijos == null || n.hijos.isEmpty()); // detecta si es hoja, si es asi se maneja diferente
        if ("float".equals(n.getTipo()) && hoja) {
            if (n.lexema != null && n.lexema.matches("-?\\d+(\\.\\d+)?")) { // literal float
                if (!floatLabels.containsKey(n.lexema)) { // si no tiene etiqueta, crea una
                    String label = newLabel("flt");
                    floatLabels.put(n.lexema, label);
                    out.println(label + ": .float " + n.lexema);
                }
            }
        }
        if (n.hijos != null) {
            for (Nodo h : n.hijos) registrarFloats(h); // recorre hijos
        }
    }

    private void cargarFloat(String freg, Nodo n) {
        
        if (n == null) return; // evita nulos
        boolean hoja = (n.hijos == null || n.hijos.isEmpty()); // detecta hoja
        if (hoja) {
            // literal float
            if (n.lexema != null && n.lexema.matches("-?\\d+(\\.\\d+)?")) {
                String label = floatLabels.get(n.lexema);
                if (label == null) { // si no existe etiqueta, crea una
                    label = newLabel("flt");
                    floatLabels.put(n.lexema, label);
                    out.println(label + ": .float " + n.lexema);
                }
                out.println("    lwc1 " + freg + ", " + label); // carga literal float
                return;
            }
            out.println("    lwc1 " + freg + ", " + direccion(n.lexema)); // carga variable float
            return;
        }

        generarExpr(n); // evalúa expresión compuesta
        out.println("    mov.s " + freg + ", $f0"); // mueve resultado a freg
    }

    private void generarResultadoFloatCmp() {
        String Ltrue = newLabel("fcmp_true"); // etiqueta para verdadero
        String Lend  = newLabel("fcmp_end");  // fin
        out.println("    bc1t " + Ltrue); // branch si comparación es true
        out.println("    li $t2, 0"); // falso
        out.println("    j " + Lend);
        out.println(Ltrue + ":");
        out.println("    li $t2, 1"); // verdadero
        out.println(Lend + ":");
    }

    private void generarResultadoFloatCmpInvertido() {
        String Ltrue = newLabel("fcmp_true"); // etiqueta para verdadero invertido
        String Lend  = newLabel("fcmp_end");  // etiqueta para fin
        out.println("    bc1f " + Ltrue); // branch si comparacion es falsa
        out.println("    li $t2, 0"); // falso
        out.println("    j " + Lend);
        out.println(Ltrue + ":");
        out.println("    li $t2, 1"); // verdadero
        out.println(Lend + ":");
    }

        // ____________________________ STRINGS ____________________________

    private void registrarStrings(Nodo n) {
        if (n == null) return; // evita nodos nulos
        if (n.lexema != null && n.lexema.startsWith("\"") && n.lexema.endsWith("\"")) { // detecta literal string
            if (!stringLabels.containsKey(n.lexema)) { // si no tiene etiqueta, crea una
                String label = newLabel("str");
                stringLabels.put(n.lexema, label);
                out.println(label + ": .asciiz " + n.lexema); // reserva string en .data
            }
        }
        if (n.hijos != null) {
            for (Nodo h : n.hijos) registrarStrings(h); // recorre hijos
        }
    }

}
