package com.joklek.fakec.parsing.error;

import com.joklek.fakec.error.Error;
import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;

public class ParserError extends RuntimeException {

    private String errorMessage;
    private Token token;

    public ParserError(String errorMessage, Token token) {
        this.errorMessage = errorMessage;
        this.token = token;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Token getToken() {
        return token;
    }
}
