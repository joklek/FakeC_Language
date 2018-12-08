package com.joklek.fakec.parsing.error;

import com.joklek.fakec.error.Error;
import com.joklek.fakec.parsing.types.data.DataType;

public class TypeError extends Error {

    private String errorMessage;
    private DataType expectedType;
    private DataType actualType;
    private int line;

    /**
     * Used when there is a concrete expected type
     * @param errorMessage error message to be given
     * @param expectedType expected type
     * @param actualType actual type
     */
    public TypeError(String errorMessage, DataType expectedType, DataType actualType) {
        this.errorMessage = errorMessage;
        this.expectedType = expectedType;
        this.actualType = actualType;

        if(actualType != null) {
            this.line = actualType.getLine();
        }
        else {
            this.line = expectedType.getLine();
        }
    }

    public TypeError(String errorMessage, DataType actualType) {
        this.errorMessage = errorMessage;
        this.expectedType = null;
        this.actualType = actualType;
        this.line = actualType.getLine();
    }

    public TypeError(String errorMessage, int line) {
        this.errorMessage = errorMessage;
        this.line = line;
    }

    public String getErrorMessage() {
        if(expectedType == null && actualType == null) {
            return errorMessage;
        }
        else if (expectedType == null && actualType != null) {
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