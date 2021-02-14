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
    : ROLL
    ;
