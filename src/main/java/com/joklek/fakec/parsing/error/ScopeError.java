package com.joklek.fakec.parsing.error;

import com.joklek.fakec.error.Error;
import com.joklek.fakec.tokens.Token;

public class ScopeError extends Error {

    private String errorMessage;
    private Token erroneousName;

    public ScopeError(String errorMessage, Token erroneousName) {
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