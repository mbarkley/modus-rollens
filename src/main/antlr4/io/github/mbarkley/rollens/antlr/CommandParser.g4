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
    : rollExpressionBasis {_localctx.setAltNumber(1);}
    | LB rollExpression RB {_localctx.setAltNumber(2);}
    | rollExpression (PLUS | MINUS | TIMES | DIVIDE) (NUMBER | REFERENCE) {_localctx.setAltNumber(3);}
    ;

rollExpressionBasis
    : DICE (PLUS DICE)* modifiers?
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
