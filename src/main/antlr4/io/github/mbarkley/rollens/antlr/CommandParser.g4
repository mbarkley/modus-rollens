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
    : DICE ((PLUS DICE)* modifiers)?
    ;

modifiers
    : successModifiers modifiers?
    | explosionModifiers modifiers?
    ;

explosionModifiers
    : ENUM
    | IENUM
    ;

successModifiers
    : TNUM
    | FNUM
    ;
