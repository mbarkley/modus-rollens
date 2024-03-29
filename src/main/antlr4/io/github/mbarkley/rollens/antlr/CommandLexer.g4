lexer grammar CommandLexer;

WHITESPACE : [ \t\r\n]+ -> channel(HIDDEN);

NUMBER : [0-9]+;

// Dice modifiers
TNUM : 't' VALUE;

FNUM : 'f' VALUE;

ENUM : 'e' VALUE;

IENUM : 'ie' VALUE;

KNUM : 'k' VALUE;

KLNUM : 'kl' VALUE;

DNUM : 'd' VALUE;

RNUM : 'r' VALUE;

IRNUM : 'ir' VALUE;

// Arithmetic symbols
TIMES : '*';

DIVIDE : '/';

PLUS : '+';

MINUS : '-';

LB : '(';

RB : ')';

EQ : '=';

// Keywords
// Dice pool symbols
fragment D : ('d' | 'D');

DICE : VALUE? D VALUE;

REFERENCE : '{' IDENTIFIER '}';

fragment VALUE : (NUMBER | REFERENCE);

fragment EXCLAMATION : '!';

START : (EXCLAMATION | DIVIDE) 'mr';

IDENTIFIER : [a-zA-Z][a-zA-Z_0-9]*;

OPEN_ANNOTATION : EXCLAMATION -> mode(FREE_TEXT);

mode FREE_TEXT;

ANNOTATION : EOF;

TEXT : . -> more;