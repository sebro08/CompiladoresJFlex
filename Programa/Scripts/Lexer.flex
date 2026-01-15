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
BLOQUE_A            = "¡"
BLOQUE_C            = "!"
PARENTESIS_A        = "¿"
PARENTESIS_B        = "?"
LLAVE_A             = "["
LLAVE_B             = "]"
COMENTARIO_UNICO    = "\|"
COMENTARIO_MULTIPLE_A = "є"
COMENTARIO_MULTIPLE_C = "э"
COMA                = ","
FLECHA              = "->"
ASIGNAR             = "="

// Expresiones Aritmeticas
SUMA                = "\+"
RESTA               = "-"
DIVISION            = "/"
DIVISION_ENTERA     = "//"
MULTIPLICACION      = "\*"
POTENCIA            = "\^"
MODULO              = "%"
AUMENTO             = "\+\+"
DISMINUCION         = "--"

// Expresiones relacionales
MAYOR               = ">"
MENOR               = "<"
MAYOR_I             = ">="
MENOR_I             = "<="
IGUAL               = "=="
DIFERENTE           = "!="

// Expresiones logicas
AND                 = "@"
OR                  = "~"
NOT                 = "Σ"

// Palabras reservadas (literales)
WORD                = "world"
LOCAL               = "local"
GIFT                = "gift"
NAVIDAD             = "navidad"
COAL                = "coal"
INT                 = "int"
FLOAT               = "float"
BOOLEAN             = "boolean"
CHAR                = "char"
STRING              = "string"
DECIDE              = "decide"
OF                  = "of"
ELSE                = "else"
END                 = "end"
LOOP                = "loop"
EXIT                = "exit"
WHEN                = "when"
FOR                 = "for"
RETURN              = "return"
BREAK               = "break"
SHOW                = "show"
GET                 = "get"
ENDL                = "endl"

// Literales numéricas
INT_LITERAL         = (0)|(-?[1-9][0-9]*)
DECIMALES           = [0-9]*[1-9]+
FLOAT_LITERAL       = (0\.0)|(-?({INT_LITERAL}\.({DECIMALES}|0))|(0\.{DECIMALES}))

// Literales de texto y char
STRING_LITERAL      = \"([^\"\\]|\\.)*\"
CHAR_LITERAL        = \'([^\'\\]|\\.)\'
BOOLEAN_LITERAL     = "true"|"false"

// Identificadores
IDENTIFICADOR          = [_a-zA-ZñÑ][_0-9a-zA-ZñÑ]*

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
<YYINITIAL> {WORD} {
    return symbol(sym.WORD, yytext());
}
<YYINITIAL> {LOCAL} {
    return symbol(sym.LOCAL, yytext());
}
<YYINITIAL> {GIFT} {
    return symbol(sym.GIFT, yytext());
}
<YYINITIAL> {NAVIDAD} {
    return symbol(sym.NAVIDAD, yytext());
}
<YYINITIAL> {COAL} {
    return symbol(sym.COAL, yytext());
}
<YYINITIAL> {INT} {
    return symbol(sym.INT, yytext());
}
<YYINITIAL> {FLOAT} {
    return symbol(sym.FLOAT, yytext());
}
<YYINITIAL> {BOOLEAN} {
    return symbol(sym.BOOLEAN, yytext());
}
<YYINITIAL> {CHAR} {
    return symbol(sym.CHAR, yytext());
}
<YYINITIAL> {STRING} {
    return symbol(sym.STRING, yytext());
}
<YYINITIAL> {DECIDE} {
    return symbol(sym.DECIDE, yytext());
}
<YYINITIAL> {OF} {
    return symbol(sym.OF, yytext());
}
<YYINITIAL> {ELSE} {
    return symbol(sym.ELSE, yytext());
}
<YYINITIAL> {END} {
    return symbol(sym.END, yytext());
}
<YYINITIAL> {LOOP} {
    return symbol(sym.LOOP, yytext());
}
<YYINITIAL> {EXIT} {
    return symbol(sym.EXIT, yytext());
}
<YYINITIAL> {WHEN} {
    return symbol(sym.WHEN, yytext());
}
<YYINITIAL> {FOR} {
    return symbol(sym.FOR, yytext());
}
<YYINITIAL> {RETURN} {
    return symbol(sym.RETURN, yytext());
}
<YYINITIAL> {BREAK} {
    return symbol(sym.BREAK, yytext());
}
<YYINITIAL> {SHOW} {
    return symbol(sym.SHOW, yytext());
}
<YYINITIAL> {GET} {
    return symbol(sym.GET, yytext());
}
<YYINITIAL> {ENDL} {
    return symbol(sym.ENDL, yytext());
}

// Operadores aritméticos
<YYINITIAL> {AUMENTO} {
    return symbol(sym.AUMENTO, yytext());
}
<YYINITIAL> {DISMINUCION} {
    return symbol(sym.DISMINUCION, yytext());
}
<YYINITIAL> {SUMA} {
    return symbol(sym.SUMA, yytext());
}
<YYINITIAL> {RESTA} {
    return symbol(sym.RESTA, yytext());
}
<YYINITIAL> {MULTIPLICACION} {
    return symbol(sym.MULTIPLICACION, yytext());
}
<YYINITIAL> {DIVISION_ENTERA} {
    return symbol(sym.DIVISION_ENTERA, yytext());
}
<YYINITIAL> {DIVISION} {
    return symbol(sym.DIVISION, yytext());
}
<YYINITIAL> {MODULO} {
    return symbol(sym.MODULO, yytext());
}
<YYINITIAL> {POTENCIA} {
    return symbol(sym.POTENCIA, yytext());
}

// Operadores relacionales
<YYINITIAL> {MAYOR_I} {
    return symbol(sym.MAYOR_IGUAL, yytext());
}
<YYINITIAL> {MENOR_I} {
    return symbol(sym.MENOR_IGUAL, yytext());
}
<YYINITIAL> {MAYOR} {
    return symbol(sym.MAYOR, yytext());
}
<YYINITIAL> {MENOR} {
    return symbol(sym.MENOR, yytext());
}
<YYINITIAL> {IGUAL} {
    return symbol(sym.IGUAL, yytext());
}
<YYINITIAL> {DIFERENTE} {
    return symbol(sym.DIFERENTE, yytext());
}

// Operadores lógicos
<YYINITIAL> {AND} {
    return symbol(sym.AND, yytext());
}
<YYINITIAL> {OR} {
    return symbol(sym.OR, yytext());
}
<YYINITIAL> {NOT} {
    return symbol(sym.NOT, yytext());
}

// Asignación y delimitadores
<YYINITIAL> {ASIGNAR} {
    return symbol(sym.ASIGNAR, yytext());
}
<YYINITIAL> {LLAVE_A} {
    return symbol(sym.LLAVE_A, yytext());
}
<YYINITIAL> {LLAVE_B} {
    return symbol(sym.LLAVE_B, yytext());
}
<YYINITIAL> {PARENTESIS_A} {
    return symbol(sym.PARENTESIS_A, yytext());
}
<YYINITIAL> {PARENTESIS_B} {
    return symbol(sym.PARENTESIS_B, yytext());
}
<YYINITIAL> {BLOQUE_A} {
    return symbol(sym.BLOQUE_A, yytext());
}
<YYINITIAL> {BLOQUE_C} {
    return symbol(sym.BLOQUE_C, yytext());
}
<YYINITIAL> {COMA} {
    return symbol(sym.COMA, yytext());
}
<YYINITIAL> {FLECHA} {
    return symbol(sym.FLECHA, yytext());
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
<YYINITIAL> {IDENTIFICADOR} {
    return symbol(sym.IDENTIFICADOR, yytext());
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
