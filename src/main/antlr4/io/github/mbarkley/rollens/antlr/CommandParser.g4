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
    ;

save
    // save (f arg1 arg2...) = <expression>
    : {getCurrentToken().getText().equals("save")}? IDENTIFIER LB IDENTIFIER (IDENTIFIER)* RB EQ roll
    ;

list
    : {getCurrentToken().getText().equals("list")}? IDENTIFIER
    ;

roll
    : ROLL modifiers?
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
