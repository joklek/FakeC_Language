package com.joklek.fakec.error;

public abstract class Error extends RuntimeException{

    public abstract String getErrorMessage();
    public abstract int getLine();
}
