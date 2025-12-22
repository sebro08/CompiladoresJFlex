import java_cup.runtime.Symbol;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

%%
%cup
%class Lexer
%unicode
%line
%column
%state STRING

%{
  private PrintWriter tokenWriter;
  private HashMap<String,String> tablaTokens = new HashMap<>();

  private Symbol symbol(int type) {
      return new Symbol(type, yyline+1, yycolumn+1);
  }

  private Symbol symbol(int type, Object value) {
      return new Symbol(type, yyline+1, yycolumn+1, value);
  }

  private void openWriter() {
      try {
          tokenWriter = new PrintWriter(new FileWriter("tokens.txt"));
      } catch (IOException e) {
          System.err.println("Error al abrir archivo tokens.txt");
          tokenWriter = null;
      }
  }

  private void closeWriter() {
      if (tokenWriter != null) tokenWriter.close();
  }
%}

%init{
  openWriter();
%init}

// Macros

// Simbolos
Bloque_A            = "¡"
Bloque_C            = "!"
Parentesis_A        = "¿"
Parentesis_B        = "?"
Comentario_Unico    = "\|"
Comentario_Multiple_A = "є"
Comentario_Multiple_C = "э"
Coma                = ","
Flecha              = "->"
Asignar             = "="

// Expresiones Aritmeticas
Suma                = "\+"
Resta               = "-"
Division            = "/"
Division_Entera     = "//"
Multiplicacion      = "\*"
Potencia            = "\^"
Modulo              = "%"
Aumento             = "\+\+"
Disminucion         = "--"

// Expresiones relacionales
Mayor               = ">"
Menor               = "<"
Mayor_I             = ">="
Menor_I             = "<="
Igual               = "=="
Diferente           = "!="

// Expresiones logicas
And                 = "@"
Or                  = "~"
Not                 = "Σ"

// Palabras reservadas (literales)
Word                = "world"
Local               = "local"
Gift                = "Gift"
Navidad             = "Navidad"
Coal                = "Coal"
Int                 = "int"
Float               = "float"
Boolean             = "boolean"
Char                = "char"
String              = "string"
Decide              = "decide"
Of                  = "of"
Else                = "else"
End                 = "end"
Loop                = "loop"
Exit                = "exit"
When                = "when"
For                 = "for"
Return              = "return"
Break               = "break"
Show                = "show"
Get                 = "get"
Endl                = "endl"

// Literales numéricas
INT_LITERAL         = (0)|(-?[1-9][0-9]*)
DECIMALES           = [0-9]*[1-9]+
FLOAT_LITERAL       = (0\.0)|(-?({INT_LITERAL}\.({DECIMALES}|0))|(0\.{DECIMALES}))

// Literales de texto y char
STRING_LITERAL      = \"([^\"\\]|\\.)*\"
CHAR_LITERAL        = \'([^\'\\]|\\.)\'
BOOLEAN_LITERAL     = "true"|"false"

// Identificadores
Identificador          = [_a-zA-ZñÑ][_0-9a-zA-ZñÑ]*

// Espacios y saltos
SPACE               = [ \t\f\r]
ENTER               = [\n]

// Comentarios 
FIN_L               = \r|\n|\r\n
INPUT1              = [^\r\n]

COMMENT_LINE        = \|{INPUT1}*{FIN_L}?
COMMENT_BLOCK       = "є"([^э])*"э"

%%

// Reglas: palabras reservadas
// Las palabras reservadas deben ir antes de Identificador
<YYINITIAL> {Word} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Word");
    if (tokenWriter != null) tokenWriter.println("Token: Word\tLexema: " + yytext());
    return symbol(sym.Word, yytext());
}
<YYINITIAL> {Local} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Local");
    if (tokenWriter != null) tokenWriter.println("Token: Local\tLexema: " + yytext());
    return symbol(sym.Local, yytext());
}
<YYINITIAL> {Gift} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Gift");
    if (tokenWriter != null) tokenWriter.println("Token: Gift\tLexema: " + yytext());
    return symbol(sym.Gift, yytext());
}
<YYINITIAL> {Navidad} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Navidad");
    if (tokenWriter != null) tokenWriter.println("Token: Navidad\tLexema: " + yytext());
    return symbol(sym.Navidad, yytext());
}
<YYINITIAL> {Coal} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Coal");
    if (tokenWriter != null) tokenWriter.println("Token: Coal\tLexema: " + yytext());
    return symbol(sym.Coal, yytext());
}
<YYINITIAL> {Int} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Int");
    if (tokenWriter != null) tokenWriter.println("Token: Int\tLexema: " + yytext());
    return symbol(sym.Int, yytext());
}
<YYINITIAL> {Float} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Float");
    if (tokenWriter != null) tokenWriter.println("Token: Float\tLexema: " + yytext());
    return symbol(sym.Float, yytext());
}
<YYINITIAL> {Boolean} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Boolean");
    if (tokenWriter != null) tokenWriter.println("Token: Boolean\tLexema: " + yytext());
    return symbol(sym.Boolean, yytext());
}
<YYINITIAL> {Char} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Char");
    if (tokenWriter != null) tokenWriter.println("Token: Char\tLexema: " + yytext());
    return symbol(sym.Char, yytext());
}
<YYINITIAL> {String} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "String");
    if (tokenWriter != null) tokenWriter.println("Token: String\tLexema: " + yytext());
    return symbol(sym.String, yytext());
}
<YYINITIAL> {Decide} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Decide");
    if (tokenWriter != null) tokenWriter.println("Token: Decide\tLexema: " + yytext());
    return symbol(sym.Decide, yytext());
}
<YYINITIAL> {Of} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Of");
    if (tokenWriter != null) tokenWriter.println("Token: Of\tLexema: " + yytext());
    return symbol(sym.Of, yytext());
}
<YYINITIAL> {Else} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Else");
    if (tokenWriter != null) tokenWriter.println("Token: Else\tLexema: " + yytext());
    return symbol(sym.Else, yytext());
}
<YYINITIAL> {End} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "End");
    if (tokenWriter != null) tokenWriter.println("Token: End\tLexema: " + yytext());
    return symbol(sym.End, yytext());
}
<YYINITIAL> {Loop} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Loop");
    if (tokenWriter != null) tokenWriter.println("Token: Loop\tLexema: " + yytext());
    return symbol(sym.Loop, yytext());
}
<YYINITIAL> {Exit} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Exit");
    if (tokenWriter != null) tokenWriter.println("Token: Exit\tLexema: " + yytext());
    return symbol(sym.Exit, yytext());
}
<YYINITIAL> {When} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "When");
    if (tokenWriter != null) tokenWriter.println("Token: When\tLexema: " + yytext());
    return symbol(sym.When, yytext());
}
<YYINITIAL> {For} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "For");
    if (tokenWriter != null) tokenWriter.println("Token: For\tLexema: " + yytext());
    return symbol(sym.For, yytext());
}
<YYINITIAL> {Return} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Return");
    if (tokenWriter != null) tokenWriter.println("Token: Return\tLexema: " + yytext());
    return symbol(sym.Return, yytext());
}
<YYINITIAL> {Break} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Break");
    if (tokenWriter != null) tokenWriter.println("Token: Break\tLexema: " + yytext());
    return symbol(sym.Break, yytext());
}
<YYINITIAL> {Show} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Show");
    if (tokenWriter != null) tokenWriter.println("Token: Show\tLexema: " + yytext());
    return symbol(sym.Show, yytext());
}
<YYINITIAL> {Get} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Get");
    if (tokenWriter != null) tokenWriter.println("Token: Get\tLexema: " + yytext());
    return symbol(sym.Get, yytext());
}
<YYINITIAL> {Endl} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Endl");
    if (tokenWriter != null) tokenWriter.println("Token: Endl\tLexema: " + yytext());
    return symbol(sym.Endl, yytext());
}

// Operadores aritméticos
<YYINITIAL> {Aumento} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Aumento");
    if (tokenWriter != null) tokenWriter.println("Token: Aumento\tLexema: " + yytext());
    return symbol(sym.Aumento, yytext());
}
<YYINITIAL> {Disminucion} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Disminucion");
    if (tokenWriter != null) tokenWriter.println("Token: Disminucion\tLexema: " + yytext());
    return symbol(sym.Disminucion, yytext());
}
<YYINITIAL> {Suma} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Suma");
    if (tokenWriter != null) tokenWriter.println("Token: PLUS\tLexema: " + yytext());
    return symbol(sym.Suma, yytext());
}
<YYINITIAL> {Resta} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Resta");
    if (tokenWriter != null) tokenWriter.println("Token: Resta\tLexema: " + yytext());
    return symbol(sym.Resta, yytext());
}
<YYINITIAL> {Multiplicacion} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Multiplicacion");
    if (tokenWriter != null) tokenWriter.println("Token: Multiplicacion\tLexema: " + yytext());
    return symbol(sym.Multiplicacion, yytext());
}
<YYINITIAL> {Division_Entera} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "DIVISION_ENTERA");
    if (tokenWriter != null) tokenWriter.println("Token: Division_Entera\tLexema: " + yytext());
    return symbol(sym.Division_Entera, yytext());
}
<YYINITIAL> {Division} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Division");
    if (tokenWriter != null) tokenWriter.println("Token: Division\tLexema: " + yytext());
    return symbol(sym.Division, yytext());
}
<YYINITIAL> {Modulo} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Modulo");
    if (tokenWriter != null) tokenWriter.println("Token: Modulo\tLexema: " + yytext());
    return symbol(sym.Modulo, yytext());
}
<YYINITIAL> {Potencia} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Potencia");
    if (tokenWriter != null) tokenWriter.println("Token: Potencia\tLexema: " + yytext());
    return symbol(sym.Potencia, yytext());
}

// Operadores relacionales
<YYINITIAL> {Mayor_I} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "MAYOR_IGUAL");
    if (tokenWriter != null) tokenWriter.println("Token: MAYOR_IGUAL\tLexema: " + yytext());
    return symbol(sym.MAYOR_IGUAL, yytext());
}
<YYINITIAL> {Menor_I} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "MENOR_IGUAL");
    if (tokenWriter != null) tokenWriter.println("Token: MENOR_IGUAL\tLexema: " + yytext());
    return symbol(sym.MENOR_IGUAL, yytext());
}
<YYINITIAL> {Mayor} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Mayor");
    if (tokenWriter != null) tokenWriter.println("Token: Mayor\tLexema: " + yytext());
    return symbol(sym.Mayor, yytext());
}
<YYINITIAL> {Menor} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Menor");
    if (tokenWriter != null) tokenWriter.println("Token: Menor\tLexema: " + yytext());
    return symbol(sym.Menor, yytext());
}
<YYINITIAL> {Igual} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Igual");
    if (tokenWriter != null) tokenWriter.println("Token: Igual\tLexema: " + yytext());
    return symbol(sym.Igual, yytext());
}
<YYINITIAL> {Diferente} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Diferente");
    if (tokenWriter != null) tokenWriter.println("Token: Diferente\tLexema: " + yytext());
    return symbol(sym.Diferente, yytext());
}

// Operadores lógicos
<YYINITIAL> {And} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "And");
    if (tokenWriter != null) tokenWriter.println("Token: And\tLexema: " + yytext());
    return symbol(sym.And, yytext());
}
<YYINITIAL> {Or} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Or");
    if (tokenWriter != null) tokenWriter.println("Token: Or\tLexema: " + yytext());
    return symbol(sym.Or, yytext());
}
<YYINITIAL> {Not} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Not");
    if (tokenWriter != null) tokenWriter.println("Token: Not\tLexema: " + yytext());
    return symbol(sym.Not, yytext());
}

// Asignación y delimitadores
<YYINITIAL> {Asignar} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Asignar");
    if (tokenWriter != null) tokenWriter.println("Token: Asignar\tLexema: " + yytext());
    return symbol(sym.Asignar, yytext());
}
<YYINITIAL> {Parentesis_A} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Parentesis_A");
    if (tokenWriter != null) tokenWriter.println("Token: Parentesis_A\tLexema: " + yytext());
    return symbol(sym.Parentesis_A, yytext());
}
<YYINITIAL> {Parentesis_B} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Parentesis_B");
    if (tokenWriter != null) tokenWriter.println("Token: Parentesis_B\tLexema: " + yytext());
    return symbol(sym.Parentesis_B, yytext());
}
<YYINITIAL> {Bloque_A} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Bloque_A");
    if (tokenWriter != null) tokenWriter.println("Token: Bloque_A\tLexema: " + yytext());
    return symbol(sym.Bloque_A, yytext());
}
<YYINITIAL> {Bloque_C} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Bloque_C");
    if (tokenWriter != null) tokenWriter.println("Token: Bloque_C\tLexema: " + yytext());
    return symbol(sym.Bloque_C, yytext());
}
<YYINITIAL> {Coma} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Coma");
    if (tokenWriter != null) tokenWriter.println("Token: Coma\tLexema: " + yytext());
    return symbol(sym.Coma, yytext());
}
<YYINITIAL> {Flecha} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "Flecha");
    if (tokenWriter != null) tokenWriter.println("Token: Flecha\tLexema: " + yytext());
    return symbol(sym.Flecha, yytext());
}

// Literales
<YYINITIAL> {INT_LITERAL} {
    if (tokenWriter != null) tokenWriter.println("Token: INT_LITERAL\tLexema: " + yytext());
    return symbol(sym.INT_LITERAL, yytext());
}
<YYINITIAL> {FLOAT_LITERAL} {
    if (tokenWriter != null) tokenWriter.println("Token: FLOAT_LITERAL\tLexema: " + yytext());
    return symbol(sym.FLOAT_LITERAL, yytext());
}
<YYINITIAL> {STRING_LITERAL} {
    if (tokenWriter != null) tokenWriter.println("Token: STRING_LITERAL\tLexema: " + yytext());
    return symbol(sym.STRING_LITERAL, yytext());
}
<YYINITIAL> {CHAR_LITERAL} {
    if (tokenWriter != null) tokenWriter.println("Token: CHAR_LITERAL\tLexema: " + yytext());
    return symbol(sym.CHAR_LITERAL, yytext());
}
<YYINITIAL> {BOOLEAN_LITERAL} {
    if (tokenWriter != null) tokenWriter.println("Token: BOOLEAN_LITERAL\tLexema: " + yytext());
    return symbol(sym.BOOLEAN_LITERAL, yytext());
}

//Identificadores
<YYINITIAL> {Identificador} {
    if (tokenWriter != null) tokenWriter.println("Token: Identificador\tLexema: " + yytext());
    return symbol(sym.Identificador, yytext());
}

// Ignorar espacios y comentarios
{SPACE}         { /* ignorar espacios y tabs */ }
{ENTER}         { /* ignorar saltos de línea (si prefieres, puedes registrar ENDL en su propia regla) */ }
{COMMENT_LINE}  { /* ignorar comentario de una línea */ }
{COMMENT_BLOCK} { /* ignorar comentario de múltiples líneas */ }

//Errores léxicos
// Se suma +1 en la impresión porque empieza en cero (línea/columna desde 1)
[^] {
    String Error = "Error léxico: " + yytext() +
                   " en la línea: " + (yyline+1) +
                   " columna: " + (yycolumn+1);
    System.out.println(Error);
}


// EOF: cerrar writer y devolver EOF
<<EOF>> {
    closeWriter();
    return symbol(sym.EOF);
}
