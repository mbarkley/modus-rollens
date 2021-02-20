parser grammar CommandParser;

options {
    tokenVocab = CommandLexer;
    contextSuperClass=org.antlr.v4.runtime.RuleContextWithAltNum;
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
    : DICE (PLUS DICE)* modifiers? constOp?
    ;

constOp
    : PLUS (NUMBER | REFERENCE) constOp?
    | MINUS (NUMBER | REFERENCE) constOp?
    | TIMES (NUMBER | REFERENCE) constOp?
    | DIVIDE (NUMBER | REFERENCE) constOp?
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
