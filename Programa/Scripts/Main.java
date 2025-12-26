import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java_cup.runtime.Symbol;

//
// java -jar ../Librerias/jflex-full-1.9.1.jar Lexer.flex
// java -jar ../Librerias/java-cup-11b.jar -parser Parser Parser.cup
// javac -cp ".:../Librerias/*" *.java
// java -cp ".:../Librerias/*" Main pruebas.txt

public class Main {
public static void main(String[] args) {
        try (
            Reader fr = new InputStreamReader(new FileInputStream(args[0]), StandardCharsets.UTF_8);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream("tokens.txt"), StandardCharsets.UTF_8))
        ) {
            Lexer lexer = new Lexer(fr);
            Symbol t;
            while (true) {
                t = lexer.next_token();
                if (t.sym == sym.EOF) break;
                writer.println("Token: " + sym.terminalNames[t.sym] + "\tLexema: " + t.value);
            }

            System.out.println("Analisis lexico finalizado.");

        } catch (IOException e) {
            System.err.println("Error de archivo: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}