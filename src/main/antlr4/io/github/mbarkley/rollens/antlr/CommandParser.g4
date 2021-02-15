parser grammar CommandParser;

options {
    tokenVocab = CommandLexer;
}

// Grammar rules
command
    : START expression EOF
    ;

expression
    : roll
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
    | TNUM FNUM
    | FNUM TNUM
    ;
