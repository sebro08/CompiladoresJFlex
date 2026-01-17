import java.util.ArrayList;

public class Nodo {
    String lexema;
    String tipo;
    ArrayList<Nodo> hijos;
    // Constructor de la clase Nodo que crea un nodo con un lexema
    public Nodo(String lexema) {
        this.lexema = lexema;
        this.tipo = "";
        this.hijos = new ArrayList<Nodo>();
    }
    // Asigna el tipo al nodo
    public void setTipo(String tipo){
        this.tipo = tipo;
    }
    // Retorna el tipo del nodo
    public String getTipo(){
        return this.tipo;
    }
    // Agrega un hijo al nodo
    public void addHijo(Nodo hijo){
        this.hijos.add(hijo);
    }
// Imprime el árbol de forma recursiva
public void arbol(){
    if (this.hijos.size() > 0){
        System.out.println("Padre: " + this.lexema);
        System.out.println("Hijos:");
        for (Nodo hijo : this.hijos) {
            System.out.println("\t" + hijo.lexema);
        }
        for (Nodo hijo : this.hijos) {
            hijo.arbol();
        }
    }
    else{
        System.out.println("Nodo: " + this.lexema);
    }
}
}
