import java_cup.runtime.Symbol;


%%
%cup
%class Lexer
%unicode
%line
%column
%state STRING

%{

  private Symbol symbol(int type) {
      return new Symbol(type, yyline+1, yycolumn+1);
  }

  private Symbol symbol(int type, Object value) {
      return new Symbol(type, yyline+1, yycolumn+1, value);
  }
%}


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
    return symbol(sym.Word, yytext());
}
<YYINITIAL> {Local} {
    return symbol(sym.Local, yytext());
}
<YYINITIAL> {Gift} {
    return symbol(sym.Gift, yytext());
}
<YYINITIAL> {Navidad} {
    return symbol(sym.Navidad, yytext());
}
<YYINITIAL> {Coal} {
    return symbol(sym.Coal, yytext());
}
<YYINITIAL> {Int} {
    return symbol(sym.Int, yytext());
}
<YYINITIAL> {Float} {
    return symbol(sym.Float, yytext());
}
<YYINITIAL> {Boolean} {
    return symbol(sym.Boolean, yytext());
}
<YYINITIAL> {Char} {
    return symbol(sym.Char, yytext());
}
<YYINITIAL> {String} {
    return symbol(sym.String, yytext());
}
<YYINITIAL> {Decide} {
    return symbol(sym.Decide, yytext());
}
<YYINITIAL> {Of} {
    return symbol(sym.Of, yytext());
}
<YYINITIAL> {Else} {
    return symbol(sym.Else, yytext());
}
<YYINITIAL> {End} {
    return symbol(sym.End, yytext());
}
<YYINITIAL> {Loop} {
    return symbol(sym.Loop, yytext());
}
<YYINITIAL> {Exit} {
    return symbol(sym.Exit, yytext());
}
<YYINITIAL> {When} {
    return symbol(sym.When, yytext());
}
<YYINITIAL> {For} {
    return symbol(sym.For, yytext());
}
<YYINITIAL> {Return} {
    return symbol(sym.Return, yytext());
}
<YYINITIAL> {Break} {
    return symbol(sym.Break, yytext());
}
<YYINITIAL> {Show} {
    return symbol(sym.Show, yytext());
}
<YYINITIAL> {Get} {
    return symbol(sym.Get, yytext());
}
<YYINITIAL> {Endl} {
    return symbol(sym.Endl, yytext());
}

// Operadores aritméticos
<YYINITIAL> {Aumento} {
    return symbol(sym.Aumento, yytext());
}
<YYINITIAL> {Disminucion} {
    return symbol(sym.Disminucion, yytext());
}
<YYINITIAL> {Suma} {
    return symbol(sym.Suma, yytext());
}
<YYINITIAL> {Resta} {
    return symbol(sym.Resta, yytext());
}
<YYINITIAL> {Multiplicacion} {
    return symbol(sym.Multiplicacion, yytext());
}
<YYINITIAL> {Division_Entera} {
    return symbol(sym.Division_Entera, yytext());
}
<YYINITIAL> {Division} {
    return symbol(sym.Division, yytext());
}
<YYINITIAL> {Modulo} {
    return symbol(sym.Modulo, yytext());
}
<YYINITIAL> {Potencia} {
    return symbol(sym.Potencia, yytext());
}

// Operadores relacionales
<YYINITIAL> {Mayor_I} {
    return symbol(sym.MAYOR_IGUAL, yytext());
}
<YYINITIAL> {Menor_I} {
    return symbol(sym.MENOR_IGUAL, yytext());
}
<YYINITIAL> {Mayor} {
    return symbol(sym.Mayor, yytext());
}
<YYINITIAL> {Menor} {
    return symbol(sym.Menor, yytext());
}
<YYINITIAL> {Igual} {
    return symbol(sym.Igual, yytext());
}
<YYINITIAL> {Diferente} {
    return symbol(sym.Diferente, yytext());
}

// Operadores lógicos
<YYINITIAL> {And} {
    return symbol(sym.And, yytext());
}
<YYINITIAL> {Or} {
    return symbol(sym.Or, yytext());
}
<YYINITIAL> {Not} {
    return symbol(sym.Not, yytext());
}

// Asignación y delimitadores
<YYINITIAL> {Asignar} {
    return symbol(sym.Asignar, yytext());
}
<YYINITIAL> {Parentesis_A} {
    return symbol(sym.Parentesis_A, yytext());
}
<YYINITIAL> {Parentesis_B} {
    return symbol(sym.Parentesis_B, yytext());
}
<YYINITIAL> {Bloque_A} {
    return symbol(sym.Bloque_A, yytext());
}
<YYINITIAL> {Bloque_C} {
    return symbol(sym.Bloque_C, yytext());
}
<YYINITIAL> {Coma} {
    return symbol(sym.Coma, yytext());
}
<YYINITIAL> {Flecha} {
    return symbol(sym.Flecha, yytext());
}

// Literales
<YYINITIAL> {INT_LITERAL} {
    return symbol(sym.INT_LITERAL, yytext());
}
<YYINITIAL> {FLOAT_LITERAL} {
    return symbol(sym.FLOAT_LITERAL, yytext());
}
<YYINITIAL> {STRING_LITERAL} {
    return symbol(sym.STRING_LITERAL, yytext());
}
<YYINITIAL> {CHAR_LITERAL} {
    return symbol(sym.CHAR_LITERAL, yytext());
}
<YYINITIAL> {BOOLEAN_LITERAL} {
    return symbol(sym.BOOLEAN_LITERAL, yytext());
}

// Identificadores
<YYINITIAL> {Identificador} {
    return symbol(sym.Identificador, yytext());
}
// Ignorar espacios y comentarios
{SPACE}         { /* ignorar espacios y tabs */ }
{ENTER}         { /* ignorar saltos de línea (si prefieres, puedes registrar ENDL en su propia regla) */ }
{COMMENT_LINE}  { /* ignorar comentario de una línea */ }
{COMMENT_BLOCK} { /* ignorar comentario de múltiples líneas */ }

//Errores léxicos
// Se suma +1 en la impresión porque empieza en cero (linea/columna desde 1)
[^] {
    String Error = "Error lexico: " + yytext() +
                   " en la linea: " + (yyline+1) +
                   " columna: " + (yycolumn+1);
    System.out.println(Error);
}
