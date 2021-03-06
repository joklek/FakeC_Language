package com.joklek.fakec.lexing.error;

import com.joklek.fakec.error.Error;

public class LexerError extends Error {

    private String errorMessage;
    private int line;

    public LexerError(String errorMessage, int line) {
        this.errorMessage = errorMessage;
        this.line = line;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }

    @Override
    public int getLine() {
        return line;
    }
}
