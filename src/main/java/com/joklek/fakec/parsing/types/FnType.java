package com.joklek.fakec.parsing.types;

public enum FnType {
    INT, CHAR, STRING, BOOL, FLOAT, VOID;

    private int line;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public DataType toDatatype() {
        DataType dataType = DataType.valueOf(this.toString());
        dataType.setLine(line);
        return dataType;
    }
}
