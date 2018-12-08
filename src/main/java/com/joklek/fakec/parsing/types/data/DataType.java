package com.joklek.fakec.parsing.types.data;

public enum DataType {
    INT, CHAR, STRING, BOOL, FLOAT, VOID, NULL;


    // TODO this is serious. As enums are static, line numbers are not unique, so this should be migrated to a class
    private int line;
    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }
}

