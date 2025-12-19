    /* JFlex example: partial Java language lexer specification */
    import java_cup.runtime.*;

    /**
     * This class is a simple example lexer.
     */
    %%

    %class Lexer
    %unicode
    %cup
    %line
    %column

    %{
      StringBuffer string = new StringBuffer();

      private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
      }
      private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
      }
    %}

    // Simbolos 
    Bloque_A = "¡"
    Bloque_C = "!"
    Parentesis_A = "¿"
    Parentesis_B = "?"
    Comentario_Unico = "|"
    Comentario_Multiple_A = "є"
    Comentario_Multiple_C = "э"
    Coma = ","
    Flecha = "->"
    Asignar = "="


    //Expresiones Aritmeticas
    Suma = "+"
    Resta = "-"
    Division = "/"
    Division_Entera = "//"
    Multiplicacion = "*"
    Potencia = "^"
    Modulo = "%"
    Aumento = "++"
    Disminucion = "--"

    //Expresiones relacionales
    Mayor = ">"
    Menor = "<"
    Mayor_I = ">="
    Menor_I = "<="
    Igual = "=="
    Diferente = "!="

    // Expresiones Logicas
    And = "@"
    Or = "~"
    Not = "Σ"

    //Palabras reservadas
    Word = "world" //  Variables Globales
    Local = "local" // Variables Locales
    Gift = "gift" // Para crear Funciones
    Navidad = "navidad" // Crea Procedimientos Iniciales (main)
    Coal = "coal" // 
    Int = "int"
    Float = "float"
    Boolean = "boolean"
    Char = "char"
    String = "string"
    Decide = "decide"
    Of = "of"
    Else = "else"
    End = "end"
    Loop = "loop"
    Exit = "exit"
    When = "when"
    For = "for"
    Return = "return"
    Break = "break"
    Show = "show"
    Get = "get"
    Endl = "endl"


    %state STRING

    %%

    /* keywords */
    <YYINITIAL> "abstract"           { return symbol(sym.ABSTRACT); }
    <YYINITIAL> "boolean"            { return symbol(sym.BOOLEAN); }
    <YYINITIAL> "break"              { return symbol(sym.BREAK); }

    <YYINITIAL> {
      /* identifiers */ 
      {Identifier}                   { return symbol(sym.IDENTIFIER); }
     
      /* literals */
      {DecIntegerLiteral}            { return symbol(sym.INTEGER_LITERAL); }
      \"                             { string.setLength(0); yybegin(STRING); }

      /* operators */
      "="                            { return symbol(sym.EQ); }
      "=="                           { return symbol(sym.EQEQ); }
      "+"                            { return symbol(sym.PLUS); }

      /* comments */
      {Comment}                      { /* ignore */ }
     
      /* whitespace */
      {WhiteSpace}                   { /* ignore */ }
    }

    <STRING> {
      \"                             { yybegin(YYINITIAL); 
                                       return symbol(sym.STRING_LITERAL, 
                                       string.toString()); }
      [^\n\r\"\\]+                   { string.append( yytext() ); }
      \\t                            { string.append('\t'); }
      \\n                            { string.append('\n'); }

      \\r                            { string.append('\r'); }
      \\\"                           { string.append('\"'); }
      \\                             { string.append('\\'); }
    }

    /* error fallback */
    [^]                              { throw new Error("Illegal character <"+
                                                        yytext()+">"); }