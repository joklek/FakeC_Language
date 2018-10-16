package com.jole;

public class LexerError {

    private String errorMessage;
    private int line;

    public LexerError(String errorMessage, int line) {
        this.errorMessage = errorMessage;
        this.line = line;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getLine() {
        return line;
    }
}
