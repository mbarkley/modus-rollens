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
    ;

save
    // save (f arg1 arg2...) = <expression>
    : {getCurrentToken().getText().equals("save")}? IDENTIFIER LB IDENTIFIER (IDENTIFIER)* RB EQ roll
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
