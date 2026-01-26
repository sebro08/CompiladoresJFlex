import java.util.*;
import java_cup.runtime.*;

public class TablaSimbolos {

    public static class SymbolInfo {
        public String nombre;
        public String tipo;
        public String categoria;
        public int linea;
        public int columna;

        // ===== NUEVO (para arreglos) =====
        public boolean esArreglo = false;
        public int filas = 0;
        public int columnasArreglo = 0;

        // Constructor normal (variables simples)
        public SymbolInfo(String n, String t, String c, int l, int col) {
            nombre = n;
            tipo = t;
            categoria = c;
            linea = l;
            columna = col;
        }

        // Constructor para arreglos
        public SymbolInfo(String n, String t, String c,
                        int filas, int columnas,
                        int l, int col) {
            nombre = n;
            tipo = t;
            categoria = c;
            this.filas = filas;
            this.columnasArreglo = columnas;
            this.esArreglo = true;
            linea = l;
            columna = col;
        }

        @Override
        public String toString() {
            if (esArreglo) {
                return nombre + " : " + tipo + "[" + filas + "][" + columnasArreglo + "]";
            }
            return nombre + " : " + tipo + " (" + categoria + ")";
        }
    }

    /* ====== TABLA DE SIMBOLOS CON SCOPES ====== */
    //Permite scopes anidados y respeta el alcance léxico.
    public static class SymbolTable {
        String scopeName;
        SymbolTable parent;
        Map<String, SymbolInfo> symbols = new LinkedHashMap<>();
        // Constructor de la tabla de simbolos que crea un nueco scope con referencia al padres
        SymbolTable(String name, SymbolTable p) {
            scopeName = name;
            parent = p;
        }
        //inserta un simbolo al scope actual 
        boolean insert(SymbolInfo s) {
            if (symbols.containsKey(s.nombre)) return false;
            symbols.put(s.nombre, s);
            return true;
        }
        //busca un simbolo en el scope actual y en los padres
        SymbolInfo lookup(String name) {
            for (SymbolTable t = this; t != null; t = t.parent) {
                if (t.symbols.containsKey(name))
                    return t.symbols.get(name);
            }
            return null;
        }
        //imprime el scope actual y sus simbolos
        void print() {
            System.out.println("Scope: " + scopeName);
            for (SymbolInfo s : symbols.values())
                System.out.println("  " + s);
        }
    }

    /* ====== MANEJO DE SCOPES ====== */
    //El scope se maneja desde el parser porque ahi se conoce la estructura del lenguaje.
    private SymbolTable globalTable = new SymbolTable("GLOBAL", null);
    private SymbolTable currentTable = globalTable;

    // Abre un nuevo scope
    public void openScope(String name) {
        currentTable = new SymbolTable(name, currentTable);
    }

    // Cierra el scope actual y vuelve al padre
    public void closeScope() {
        currentTable.print();
        currentTable = currentTable.parent;
    }
     // Declara un identificador en el scope actual
    public void declare(String name, String type, String cat, Symbol id) {
        boolean ok = currentTable.insert(
            new SymbolInfo(name, type, cat, id.left, id.right)
        );
        if (!ok) {
            System.err.println(
                "Error semántico: '" + name +
                "' redeclarado (línea " + id.left + ")"
            );
        }
    }
    //Verifica que un identificador haya sido declarado
    public void use(String name, Symbol id) {
        if (currentTable.lookup(name) == null) {
            System.err.println(
                "Error semántico: '" + name +
                "' no declarado (línea " + id.left + ")"
            );
        }
    }
    // Imprime solo la tabla global
    public void printGlobalTable() {
        globalTable.print();
    }
        // Devuelve la información de un símbolo (buscando en el scope actual y padres)
    public SymbolInfo lookup(String name) {
        return currentTable.lookup(name);
    }

    // Devuelve todos los símbolos globales (para el generador)
    public Collection<SymbolInfo> getGlobalSymbols() {
        return globalTable.symbols.values();
    }

}
