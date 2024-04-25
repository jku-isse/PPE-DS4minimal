package at.jku.isse.designspace.rule.arl.parser;

import ParsingException;

/**
 * This class is a lexical scanner for ARL input
 */

%%

%class ArlScanner
%unicode
%line
%column
%char
%public
%eofclose
%type ArlToken

%{
    private StringBuffer string = new StringBuffer();
    private int stringPos = 0;

    private ArlToken createToken(int kind, String stringValue) {
        return createToken(kind, 0, 0.0, stringValue);
    }

    private ArlToken createToken(int kind, long longValue) {
        return createToken(kind, longValue, 0.0, null);
    }

    private ArlToken createToken(int kind, double doubleValue) {
        return createToken(kind, 0, doubleValue, null);
    }

    private ArlToken createToken(int kind) {
        return createToken(kind, 0, 0.0, null);
    }

    private ArlToken createToken(int kind, long longValue, double doubleValue, String stringValue) {
        return createToken(kind, yychar, yylength(), longValue, doubleValue, stringValue);

    }

    private ArlToken createToken(int kind, int pos, int length, long longValue, double doubleValue, String stringValue) {
        return new ArlToken(kind, pos, length, longValue, doubleValue, stringValue);
    }

    public int getLine() {
        return yyline;
    }

    public int getColumn() {
        return yycolumn;
    }

    private void error() {
        throw new ParsingException(yyline, yycolumn, "illegal character '%s' while parsing", yytext());
    }


%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

Comment =  "--" {InputCharacter}* {LineTerminator}

Character = [a-zA-Z_0-9]
DecDigit = [0-9]
Sign = [-]
SimpleName = {Character}+
IntegerLiteral = {Sign}? {DecDigit}+
RealLiteral = {Sign}? {DecDigit} + "." {DecDigit}+ ? ([eE] {Sign}? {DecDigit}+ )?
SimpleOperator = [\+\-\*/=\<\>\.,:;\{\}\(\)\[\]|@\?\^]
StringTerminator = [\"\']

%state STRING

%%

/* keywords */
<YYINITIAL> {

  {Comment}                     { /* ignore */ }

  {StringTerminator}            { string.setLength(0); stringPos = yychar; yybegin(STRING); }

  {WhiteSpace}                  { /* ignore */ }

  "not"                         { return createToken(ArlTokenKind.NOT); }
  "implies"                     { return createToken(ArlTokenKind.IMPLIES); }
  "xor"                         { return createToken(ArlTokenKind.XOR); }
  "or"                          { return createToken(ArlTokenKind.OR); }
  "and"                         { return createToken(ArlTokenKind.AND); }
  "<>"                          { return createToken(ArlTokenKind.NEQ); }
  "<="                          { return createToken(ArlTokenKind.LEQ); }
  ">="                          { return createToken(ArlTokenKind.GEQ); }
  "->"                          { return createToken(ArlTokenKind.ARROW); }
  "::"                          { return createToken(ArlTokenKind.DOUBLECOLON); }
  ".."                          { return createToken(ArlTokenKind.DOUBLEDOT); }
  "^^"                          { return createToken(ArlTokenKind.DOUBLEHAT); }
  "Set"                         { return createToken(ArlTokenKind.SET); }
  "List"                        { return createToken(ArlTokenKind.LIST); }
  "Map"                         { return createToken(ArlTokenKind.MAP); }
  "Collection"                  { return createToken(ArlTokenKind.COLLECTION); }
  "Tuple"                       { return createToken(ArlTokenKind.TUPLE); }
  "true"                        { return createToken(ArlTokenKind.TRUE); }
  "false"                       { return createToken(ArlTokenKind.FALSE); }
  "null"                        { return createToken(ArlTokenKind.NULL); }
  "let"                         { return createToken(ArlTokenKind.LET); }
  "def"                         { return createToken(ArlTokenKind.DEF); }
  "in"                          { return createToken(ArlTokenKind.IN); }
  "if"                          { return createToken(ArlTokenKind.IF); }
  "then"                        { return createToken(ArlTokenKind.THEN); }
  "else"                        { return createToken(ArlTokenKind.ELSE); }
  "endif"                       { return createToken(ArlTokenKind.ENDIF); }
  "iterate"                     { return createToken(ArlTokenKind.ITERATE); }
  "forAll"						{ return createToken(ArlTokenKind.FORALL); }
  "exists"						{ return createToken(ArlTokenKind.EXISTS); }
  "collect"						{ return createToken(ArlTokenKind.COLLECT); }
  "reject"						{ return createToken(ArlTokenKind.REJECT); }
  "select"						{ return createToken(ArlTokenKind.SELECT); }
  "next"						{ return createToken(ArlTokenKind.NEXT); }
  "eventually"					{ return createToken(ArlTokenKind.EVENTUALLY); }
  "always"						{ return createToken(ArlTokenKind.ALWAYS); }
  "until"						{ return createToken(ArlTokenKind.UNTIL); }
  "asSoonAs"                    { return createToken(ArlTokenKind.ASSOONAS); }
  "everytime"                   { return createToken(ArlTokenKind.EVERYTIME); }

  {SimpleOperator}              { return createToken(yytext().charAt(0)); }

  {IntegerLiteral}              { return createToken(ArlTokenKind.INTEGER, Long.parseLong(yytext())); }

  {RealLiteral}                 { return createToken(ArlTokenKind.REAL, Double.parseDouble(yytext())); }

  {SimpleName}                  { return createToken(ArlTokenKind.NAME, yytext()); }


}

<STRING> {
  {StringTerminator}             { yybegin(YYINITIAL); return createToken(ArlTokenKind.STRING, stringPos, yychar-stringPos+yytext().length(), 0, 0.0, string.toString()); }
  [^\n\r\"\'\\]+                 { string.append( yytext() ); }
  \\t                            { string.append('\t'); }
  \\n                            { string.append('\n'); }

  \\r                            { string.append('\r'); }
  \\\"                           { string.append('\"'); }
  \\'                            { string.append('\''); }
  \\\\                           { string.append('\\'); }
}

<<EOF>>                          { return createToken(ArlTokenKind.ENDINPUT); }

/* error fallback */
.|\n                             { error();  }
