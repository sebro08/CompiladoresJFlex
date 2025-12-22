import java.io.FileReader;
import java_cup.runtime.Symbol;

//java -jar ../Librerias/jflex-full-1.9.1.jar ./Lexer.flex
//java -jar ../Librerias/java-cup-11b.jar -parser Parser Parser.cup
//javac -cp ".:../Librerias/*" *.java
//java -cp ".:../Librerias/*" Main pruebas.txt

public class Main{
  public static void main(String[] args) {
    try {
      Lexer lexer = new Lexer(new FileReader(args[0]));
      Symbol t;
      do {
        t = lexer.next_token();
      } while (t.sym != sym.EOF);
      System.out.println("Analisis lexico finalizado.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
