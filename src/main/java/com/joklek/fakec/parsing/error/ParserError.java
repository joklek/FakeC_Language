package com.joklek.fakec.parsing.error;

import com.joklek.fakec.error.Error;
import com.joklek.fakec.tokens.Token;

public class ParserError extends Error {

    private String errorMessage;
    private Token token;

    public ParserError(String errorMessage, Token token) {
        this.errorMessage = errorMessage;
        this.token = token;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public int getLine() {
        return token.getLine();
    }

    public Token getToken() {
        return token;
    }
}
