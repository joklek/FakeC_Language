package com.joklek.fakec.parsing.types;

public enum DataType {
    INT, CHAR, STRING, BOOL, FLOAT, VOID, NULL;

    private int line;
    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }
}

