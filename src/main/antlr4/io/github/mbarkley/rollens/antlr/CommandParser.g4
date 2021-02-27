parser grammar CommandParser;

options {
    tokenVocab = CommandLexer;
    contextSuperClass=io.github.mbarkley.rollens.antlr.RuleContextWithAltNum;
}

// Grammar rules
command
    : START expression EOF
    ;

expression
    : roll
    | save
    | list
    | delete
    | help
    | invocation
    ;

delete
    // delete name <arity>
    : {getCurrentToken().getText().equals("delete")}? IDENTIFIER IDENTIFIER NUMBER
    ;

invocation
    : IDENTIFIER (NUMBER)*
    ;

save
    // save (f arg1 arg2...) = <expression>
    : {getCurrentToken().getText().equals("save")}? IDENTIFIER LB IDENTIFIER (IDENTIFIER)* RB EQ roll
    ;

help
    : {getCurrentToken().getText().equals("help")}? IDENTIFIER
    ;

list
    : {getCurrentToken().getText().equals("list")}? IDENTIFIER
    ;

roll
    : rollExpression
    ;

// Set alt numbers manually as workaround for bug
rollExpression
    : numeric rollExpression {_localctx.setAltNumber(1);}
    | rollExpression unaryConstantOp {_localctx.setAltNumber(2);}
    | LB rollExpression RB {_localctx.setAltNumber(3);}
    | rollExpression op=(DIVIDE|TIMES) rollExpression {_localctx.setAltNumber(4);}
    | rollExpression op=(PLUS|MINUS) rollExpression {_localctx.setAltNumber(5);}
    | simpleRoll {_localctx.setAltNumber(6);}
    ;

unaryConstantOp
    : op=(TIMES|DIVIDE) numeric {_localctx.setAltNumber(1);}
    | op=(TIMES|DIVIDE) LB constExpression RB {_localctx.setAltNumber(2);}
    | op=(TIMES|DIVIDE) constExpression op2=(DIVIDE|TIMES) constExpression {_localctx.setAltNumber(3);}
    | op=(TIMES|DIVIDE) constExpression op2=(PLUS|MINUS) constExpression {_localctx.setAltNumber(4);}
    | op=(PLUS|MINUS) constExpression {_localctx.setAltNumber(5);}
    ;

constExpression
    : numeric {_localctx.setAltNumber(1);}
    | LB constExpression RB {_localctx.setAltNumber(2);}
    | constExpression op=(DIVIDE|TIMES) constExpression {_localctx.setAltNumber(3);}
    | constExpression op=(PLUS|MINUS) constExpression {_localctx.setAltNumber(4);}
    ;

numeric
    : NUMBER
    | REFERENCE
    ;

simpleRoll
    // Make sure we can't parse strings like `2d6 + 1d10` with this pattern
    // Only parse sums of dice as single term if there are modifiers (ex. `d6 + d4 e4`)
    // Otherwise we break precedence of operations
    : dice ((PLUS dice)* modifiers)?
    ;

dice
    : DICE
    | DNUM
    ;

modifiers
    : modifier+
    ;

modifier
    : TNUM {_localctx.setAltNumber(1);}
    | FNUM {_localctx.setAltNumber(2);}
    | ENUM {_localctx.setAltNumber(3);}
    | IENUM {_localctx.setAltNumber(4);}
    | KNUM {_localctx.setAltNumber(5);}
    | DNUM {_localctx.setAltNumber(6);}
    | RNUM {_localctx.setAltNumber(7);}
    | IRNUM {_localctx.setAltNumber(8);}
    | KLNUM {_localctx.setAltNumber(9);}
    ;
