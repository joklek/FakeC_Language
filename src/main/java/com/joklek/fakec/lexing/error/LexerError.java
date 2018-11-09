package com.joklek.fakec.lexing.error;

import com.joklek.fakec.error.Error;

public class LexerError implements Error {

    private String errorMessage;
    private int line;

    public LexerError(String errorMessage, int line) {
        this.errorMessage = errorMessage;
        this.line = line;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public int getLine() {
        return line;
    }
}
