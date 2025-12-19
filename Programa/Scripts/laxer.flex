

%{
import java_cup.runtime.Symbol;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

// Writer y tabla para registrar tokens 
PrintWriter tokenWriter;
HashMap<String,String> tablaTokens = new HashMap<>();

// Inicializador: abre el archivo de tokens
{
    try {
        tokenWriter = new PrintWriter(new FileWriter("tokens.txt"));
    } catch (IOException e) {
        System.err.println("Error al abrir archivo tokens.txt");
        tokenWriter = null;
    }
}

// Cierra el writer (se invoca al final con <<EOF>> )
public void closeWriter() {
    if (tokenWriter != null) tokenWriter.close();
}

// Auxiliares para crear Symbols
private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
}

private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
}
%}

/* Configuración JFlex */
%cup
%class Lexer
%unicode
%line
%column
%state STRING

%%

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
Gift                = "gift"
Navidad             = "navidad"
Coal                = "coal"
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
IDENTIFIER          = [_a-zA-ZñÑ][_0-9a-zA-ZñÑ]*

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
// Las palabras reservadas deben ir antes de IDENTIFIER
<YYINITIAL> {Word} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "WORD");
    if (tokenWriter != null) tokenWriter.println("Token: WORD\tLexema: " + yytext());
    return symbol(sym.WORD, yytext());
}
<YYINITIAL> {Local} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "LOCAL");
    if (tokenWriter != null) tokenWriter.println("Token: LOCAL\tLexema: " + yytext());
    return symbol(sym.LOCAL, yytext());
}
<YYINITIAL> {Gift} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "GIFT");
    if (tokenWriter != null) tokenWriter.println("Token: GIFT\tLexema: " + yytext());
    return symbol(sym.GIFT, yytext());
}
<YYINITIAL> {Navidad} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "NAVIDAD");
    if (tokenWriter != null) tokenWriter.println("Token: NAVIDAD\tLexema: " + yytext());
    return symbol(sym.NAVIDAD, yytext());
}
<YYINITIAL> {Coal} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "COAL");
    if (tokenWriter != null) tokenWriter.println("Token: COAL\tLexema: " + yytext());
    return symbol(sym.COAL, yytext());
}
<YYINITIAL> {Int} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "INT");
    if (tokenWriter != null) tokenWriter.println("Token: INT\tLexema: " + yytext());
    return symbol(sym.INT, yytext());
}
<YYINITIAL> {Float} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "FLOAT");
    if (tokenWriter != null) tokenWriter.println("Token: FLOAT\tLexema: " + yytext());
    return symbol(sym.FLOAT, yytext());
}
<YYINITIAL> {Boolean} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "BOOLEAN");
    if (tokenWriter != null) tokenWriter.println("Token: BOOLEAN\tLexema: " + yytext());
    return symbol(sym.BOOLEAN, yytext());
}
<YYINITIAL> {Char} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "CHAR");
    if (tokenWriter != null) tokenWriter.println("Token: CHAR\tLexema: " + yytext());
    return symbol(sym.CHAR, yytext());
}
<YYINITIAL> {String} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "STRING");
    if (tokenWriter != null) tokenWriter.println("Token: STRING\tLexema: " + yytext());
    return symbol(sym.STRING, yytext());
}
<YYINITIAL> {Decide} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "DECIDE");
    if (tokenWriter != null) tokenWriter.println("Token: DECIDE\tLexema: " + yytext());
    return symbol(sym.DECIDE, yytext());
}
<YYINITIAL> {Of} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "OF");
    if (tokenWriter != null) tokenWriter.println("Token: OF\tLexema: " + yytext());
    return symbol(sym.OF, yytext());
}
<YYINITIAL> {Else} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "ELSE");
    if (tokenWriter != null) tokenWriter.println("Token: ELSE\tLexema: " + yytext());
    return symbol(sym.ELSE, yytext());
}
<YYINITIAL> {End} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "END");
    if (tokenWriter != null) tokenWriter.println("Token: END\tLexema: " + yytext());
    return symbol(sym.END_C, yytext());
}
<YYINITIAL> {Loop} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "LOOP");
    if (tokenWriter != null) tokenWriter.println("Token: LOOP\tLexema: " + yytext());
    return symbol(sym.LOOP, yytext());
}
<YYINITIAL> {Exit} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "EXIT");
    if (tokenWriter != null) tokenWriter.println("Token: EXIT\tLexema: " + yytext());
    return symbol(sym.EXIT, yytext());
}
<YYINITIAL> {When} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "WHEN");
    if (tokenWriter != null) tokenWriter.println("Token: WHEN\tLexema: " + yytext());
    return symbol(sym.WHEN, yytext());
}
<YYINITIAL> {For} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "FOR");
    if (tokenWriter != null) tokenWriter.println("Token: FOR\tLexema: " + yytext());
    return symbol(sym.FOR, yytext());
}
<YYINITIAL> {Return} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "RETURN");
    if (tokenWriter != null) tokenWriter.println("Token: RETURN\tLexema: " + yytext());
    return symbol(sym.RETURN, yytext());
}
<YYINITIAL> {Break} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "BREAK");
    if (tokenWriter != null) tokenWriter.println("Token: BREAK\tLexema: " + yytext());
    return symbol(sym.BREAK, yytext());
}
<YYINITIAL> {Show} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "SHOW");
    if (tokenWriter != null) tokenWriter.println("Token: SHOW\tLexema: " + yytext());
    return symbol(sym.SHOW, yytext());
}
<YYINITIAL> {Get} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "GET");
    if (tokenWriter != null) tokenWriter.println("Token: GET\tLexema: " + yytext());
    return symbol(sym.GET, yytext());
}
<YYINITIAL> {Endl} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "ENDL");
    if (tokenWriter != null) tokenWriter.println("Token: ENDL\tLexema: " + yytext());
    return symbol(sym.ENDL, yytext());
}

// Operadores aritméticos
<YYINITIAL> {Aumento} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "AUMENTO");
    if (tokenWriter != null) tokenWriter.println("Token: AUMENTO\tLexema: " + yytext());
    return symbol(sym.INC, yytext());
}
<YYINITIAL> {Disminucion} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "DISMINUCION");
    if (tokenWriter != null) tokenWriter.println("Token: DISMINUCION\tLexema: " + yytext());
    return symbol(sym.DEC, yytext());
}
<YYINITIAL> {Suma} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "SUMA");
    if (tokenWriter != null) tokenWriter.println("Token: PLUS\tLexema: " + yytext());
    return symbol(sym.PLUS, yytext());
}
<YYINITIAL> {Resta} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "RESTA");
    if (tokenWriter != null) tokenWriter.println("Token: MINUS\tLexema: " + yytext());
    return symbol(sym.MINUS, yytext());
}
<YYINITIAL> {Multiplicacion} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "MULTIPLICACION");
    if (tokenWriter != null) tokenWriter.println("Token: TIMES\tLexema: " + yytext());
    return symbol(sym.TIMES, yytext());
}
<YYINITIAL> {Division_Entera} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "DIVISION_ENTERA");
    if (tokenWriter != null) tokenWriter.println("Token: IDIV\tLexema: " + yytext());
    return symbol(sym.IDIV, yytext());
}
<YYINITIAL> {Division} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "DIVISION");
    if (tokenWriter != null) tokenWriter.println("Token: DIV\tLexema: " + yytext());
    return symbol(sym.DIV, yytext());
}
<YYINITIAL> {Modulo} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "MODULO");
    if (tokenWriter != null) tokenWriter.println("Token: MOD\tLexema: " + yytext());
    return symbol(sym.MOD, yytext());
}
<YYINITIAL> {Potencia} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "POTENCIA");
    if (tokenWriter != null) tokenWriter.println("Token: POW\tLexema: " + yytext());
    return symbol(sym.POW, yytext());
}

// Operadores relacionales
<YYINITIAL> {Mayor_I} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "MAYOR_IGUAL");
    if (tokenWriter != null) tokenWriter.println("Token: GE\tLexema: " + yytext());
    return symbol(sym.GE, yytext());
}
<YYINITIAL> {Menor_I} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "MENOR_IGUAL");
    if (tokenWriter != null) tokenWriter.println("Token: LE\tLexema: " + yytext());
    return symbol(sym.LE, yytext());
}
<YYINITIAL> {Mayor} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "MAYOR");
    if (tokenWriter != null) tokenWriter.println("Token: GT\tLexema: " + yytext());
    return symbol(sym.GT, yytext());
}
<YYINITIAL> {Menor} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "MENOR");
    if (tokenWriter != null) tokenWriter.println("Token: LT\tLexema: " + yytext());
    return symbol(sym.LT, yytext());
}
<YYINITIAL> {Igual} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "IGUAL");
    if (tokenWriter != null) tokenWriter.println("Token: EQ\tLexema: " + yytext());
    return symbol(sym.EQ, yytext());
}
<YYINITIAL> {Diferente} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "DIFERENTE");
    if (tokenWriter != null) tokenWriter.println("Token: NE\tLexema: " + yytext());
    return symbol(sym.NE, yytext());
}

// Operadores lógicos
<YYINITIAL> {And} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "AND");
    if (tokenWriter != null) tokenWriter.println("Token: AND\tLexema: " + yytext());
    return symbol(sym.AND, yytext());
}
<YYINITIAL> {Or} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "OR");
    if (tokenWriter != null) tokenWriter.println("Token: OR\tLexema: " + yytext());
    return symbol(sym.OR, yytext());
}
<YYINITIAL> {Not} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "NOT");
    if (tokenWriter != null) tokenWriter.println("Token: NOT\tLexema: " + yytext());
    return symbol(sym.NOT, yytext());
}

// Asignación y delimitadores
<YYINITIAL> {Asignar} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "ASIGNAR");
    if (tokenWriter != null) tokenWriter.println("Token: ASSIGN\tLexema: " + yytext());
    return symbol(sym.ASSIGN, yytext());
}
<YYINITIAL> {Parentesis_A} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "LPAREN");
    if (tokenWriter != null) tokenWriter.println("Token: LPAREN\tLexema: " + yytext());
    return symbol(sym.LPAREN, yytext());
}
<YYINITIAL> {Parentesis_B} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "RPAREN");
    if (tokenWriter != null) tokenWriter.println("Token: RPAREN\tLexema: " + yytext());
    return symbol(sym.RPAREN, yytext());
}
<YYINITIAL> {Bloque_A} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "LBRACE");
    if (tokenWriter != null) tokenWriter.println("Token: LBRACE\tLexema: " + yytext());
    return symbol(sym.LBRACE, yytext());
}
<YYINITIAL> {Bloque_C} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "RBRACE");
    if (tokenWriter != null) tokenWriter.println("Token: RBRACE\tLexema: " + yytext());
    return symbol(sym.RBRACE, yytext());
}
<YYINITIAL> {Coma} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "COMA");
    if (tokenWriter != null) tokenWriter.println("Token: COMA\tLexema: " + yytext());
    return symbol(sym.COMA, yytext());
}
<YYINITIAL> {Flecha} {
    if (!tablaTokens.containsKey(yytext())) tablaTokens.put(yytext(), "FLECHA");
    if (tokenWriter != null) tokenWriter.println("Token: FLECHA\tLexema: " + yytext());
    return symbol(sym.FLECHA, yytext());
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
<YYINITIAL> {IDENTIFIER} {
    if (tokenWriter != null) tokenWriter.println("Token: IDENTIFIER\tLexema: " + yytext());
    return symbol(sym.IDENTIFIER, yytext());
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
