lexer grammar CommandLexer;

WHITESPACE : (' ' | '\t')+ -> channel(HIDDEN);

NUMBER : [0-9]+;

// Dice modifiers
TNUM : 't' VALUE;

FNUM : 'f' VALUE;

ENUM : 'e' VALUE;

IENUM : 'ie' VALUE;

KNUM : 'k' VALUE;

DNUM : 'd' VALUE;

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

EXCLAMATION : '!';

START : EXCLAMATION 'mr';

IDENTIFIER : [a-zA-Z][a-zA-Z_0-9]*;