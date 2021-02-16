lexer grammar CommandLexer;

WHITESPACE : (' ' | '\t')+ -> channel(HIDDEN);

NUMBER : [0-9]+;

// Dice modifiers
TNUM : 't' VALUE;

FNUM : 'f' VALUE;

ENUM : 'e' VALUE;

IENUM : 'ie' VALUE;

// Arithmetic symbols
TIMES : '*';

DIVIDE : '/';

MOD : '%';

PLUS : '+';

MINUS : '-';

LB : '(';

RB : ')';

LSB : '[';

RSB : ']';

// Relation symbols
LT : '<';

GT : '>';

LTE : '<=';

GTE : '>=';

EQ : '=';

// Keywords
// Dice pool symbols
fragment D : ('d' | 'D');

ROLL : VALUE? D VALUE;

VALUE : (NUMBER | '{' IDENTIFIER '}');

EXCLAMATION : '!';

START : EXCLAMATION 'mr';

IDENTIFIER : [a-zA-Z][a-zA-Z_0-9]*;