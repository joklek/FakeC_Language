package com.joklek.fakec.parsing.error;

import com.joklek.fakec.error.Error;
import com.joklek.fakec.tokens.Token;

public class TypeError extends Error {

    private String errorMessage;
    private Token erroneousName;

    public TypeError(String errorMessage, Token erroneousName) {
        this.errorMessage = errorMessage;
        this.erroneousName = erroneousName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public int getLine() {
        return erroneousName.getLine();
    }

    public Token getErroneousName() {
        return erroneousName;
    }
}