import java.util.ArrayList;

public class Nodo {
    String lexema;
    String tipo;
    ArrayList<Nodo> hijos;

    public Nodo(String lexema) {
        this.lexema = lexema;
        this.tipo = "";
        this.hijos = new ArrayList<Nodo>();
    }

    public void setTipo(String tipo){
        this.tipo = tipo;
    }

    public String getTipo(){
        return this.tipo;
    }

    public void addHijo(Nodo hijo){
        this.hijos.add(hijo);
    }

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
