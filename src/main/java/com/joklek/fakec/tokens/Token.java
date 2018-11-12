package com.joklek.fakec.tokens;

import java.util.Objects;

public class Token {
    private final TokenType type;
    private final String lexeme;
    private final Object literal;
    private final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public Token(TokenType type, int line) {
        this.type = type;
        this.lexeme = null;
        this.literal = null;
        this.line = line;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public Object getLiteral() {
        return literal;
    }

    public int getLine() {
        return line;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token)) return false;
        Token token = (Token) o;
        return getLine() == token.getLine() &&
                getType() == token.getType() &&
                Objects.equals(getLexeme(), token.getLexeme()) &&
                Objects.equals(getLiteral(), token.getLiteral());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getLexeme(), getLiteral(), getLine());
    }
}
