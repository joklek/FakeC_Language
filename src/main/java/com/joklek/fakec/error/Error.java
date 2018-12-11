package com.joklek.fakec.error;

public abstract class Error extends RuntimeException{

    public abstract int getLine();
}
