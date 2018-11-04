package com.jole.tokens;

@SuppressWarnings("squid:CommentedOutCodeLine")
public enum TokenType {
    // Tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, CURLY_LEFT, CURLY_RIGHT, // ()[]{}
    COMMA, MINUS, PLUS, SEMICOLON, SLASH, STAR, MOD, NOT,                 // .,-+;/*%!

    EQUAL, EQUAL_EQUAL,     // = ==
    GREATER, GREATER_EQUAL, // > >=
    LESS, LESS_EQUAL,       // < <=
    PLUS_EQUAL, MINUS_EQUAL, MUL_EQUAL, DIV_EQUAL, MOD_EQUAL, NOT_EQUAL, // += -= *= /= %=

    // Literals
    IDENTIFIER, STRING, INTEGER, FLOAT, CHAR,

    // Keywords
    AND, OR, RETURN, BREAK, CONTINUE, WHILE, FOR, IF, ELSE, TRUE, FALSE, NULL,
    FLOAT_TYPE, INT_TYPE, CHAR_TYPE, BOOL_TYPE, STRING_TYPE, VOID_TYPE,

    // IO
    INPUT, OUTPUT, INPUT_SIGN, OUTPUT_SIGN,

    EOF
}
