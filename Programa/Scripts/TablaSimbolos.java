import java.util.*;
import java_cup.runtime.*;

public class TablaSimbolos {

    /* ====== ENTRADA DE TABLA ====== */
    public static class SymbolInfo {
        public String nombre;
        public String tipo;
        public String categoria;
        public int linea;
        public int columna;

        public SymbolInfo(String n, String t, String c, int l, int col) {
            nombre = n;
            tipo = t;
            categoria = c;
            linea = l;
            columna = col;
        }

        @Override
        public String toString() {
            return nombre + " : " + tipo + " (" + categoria + ")";
        }
    }

    /* ====== TABLA DE SÍMBOLOS CON SCOPES ====== */
    public static class SymbolTable {
        String scopeName;
        SymbolTable parent;
        Map<String, SymbolInfo> symbols = new LinkedHashMap<>();

        SymbolTable(String name, SymbolTable p) {
            scopeName = name;
            parent = p;
        }

        boolean insert(SymbolInfo s) {
            if (symbols.containsKey(s.nombre)) return false;
            symbols.put(s.nombre, s);
            return true;
        }

        SymbolInfo lookup(String name) {
            for (SymbolTable t = this; t != null; t = t.parent) {
                if (t.symbols.containsKey(name))
                    return t.symbols.get(name);
            }
            return null;
        }

        void print() {
            System.out.println("Scope: " + scopeName);
            for (SymbolInfo s : symbols.values())
                System.out.println("  " + s);
        }
    }

    /* ====== MANEJO DE SCOPES ====== */
    private SymbolTable globalTable = new SymbolTable("GLOBAL", null);
    private SymbolTable currentTable = globalTable;

    public void openScope(String name) {
        currentTable = new SymbolTable(name, currentTable);
    }

    public void closeScope() {
        currentTable.print();
        currentTable = currentTable.parent;
    }

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

    public void use(String name, Symbol id) {
        if (currentTable.lookup(name) == null) {
            System.err.println(
                "Error semántico: '" + name +
                "' no declarado (línea " + id.left + ")"
            );
        }
    }

    public void printGlobalTable() {
        globalTable.print();
    }
}
