package com.joklek.fakec.parsing.error;

import com.joklek.fakec.error.Error;
import com.joklek.fakec.parsing.types.data.DataType;

public class TypeError extends Error {

    private String errorMessage;
    private DataType expectedType;
    private DataType actualType;

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
    }

    public TypeError(String errorMessage, DataType actualType) {
        this.errorMessage = errorMessage;
        this.expectedType = null;
        this.actualType = actualType;
    }

    public String getErrorMessage() {
        return errorMessage + String.format(". Expected '%s', but got '%s'", expectedType, actualType);
    }

    @Override
    public int getLine() {
        if(actualType != null) {
            return actualType.getLine();
        }
        return expectedType.getLine();
    }
}