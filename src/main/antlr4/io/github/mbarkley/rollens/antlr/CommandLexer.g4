lexer grammar CommandLexer;

WHITESPACE : (' ' | '\t')+ -> skip;

// Keywords
NUMBER : [0-9]+;

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

// Dice pool symbols
fragment D : ('d' | 'D');

ROLL : NUMBER? D NUMBER;

EXCLAMATION : '!';

START : EXCLAMATION 'mr';