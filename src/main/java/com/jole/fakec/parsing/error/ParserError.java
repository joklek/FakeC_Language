package com.jole.fakec.parsing.error;

import com.jole.fakec.error.Error;

public class ParserError implements Error {

    private String errorMessage;
    private int line;

    public ParserError(String errorMessage, int line) {
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
