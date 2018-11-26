package com.joklek.fakec.parsing.error;

public class ScopeError extends RuntimeException {

    private String errorMessage;
    private String erroneousName;

    public ScopeError(String errorMessage, String erroneousName) {
        this.errorMessage = errorMessage;
        this.erroneousName = erroneousName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErroneousName() {
        return erroneousName;
    }
}