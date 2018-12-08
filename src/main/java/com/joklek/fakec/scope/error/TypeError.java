package com.joklek.fakec.scope.error;

import com.joklek.fakec.error.Error;
import com.joklek.fakec.parsing.types.data.DataType;

public class TypeError extends Error {

    private final String errorMessage;
    private final DataType expectedType;
    private final DataType actualType;
    private final int line;

    /**
     * Used when there is a concrete expected type
     * @param errorMessage error message to be given
     * @param expectedType expected type
     * @param actualType actual type
     */
    public TypeError(String errorMessage, DataType expectedType, DataType actualType, int line) {
        this.errorMessage = errorMessage;
        this.expectedType = expectedType;
        this.actualType = actualType;
        this.line = line;
    }

    public TypeError(String errorMessage, DataType actualType, int line) {
        this.errorMessage = errorMessage;
        this.expectedType = null;
        this.actualType = actualType;
        this.line = line;
    }

    public TypeError(String errorMessage, int line) {
        this.errorMessage = errorMessage;
        this.line = line;
        this.expectedType = null;
        this.actualType = null;
    }

    public String getErrorMessage() {
        if(expectedType == null && actualType == null) {
            return errorMessage;
        }
        else if (expectedType == null) {
            return errorMessage + ". " + actualType;
        }
        else {
            return errorMessage + String.format(". Expected '%s', but got '%s'", expectedType, actualType);
        }
    }

    @Override
    public int getLine() {
        return line;
    }
}