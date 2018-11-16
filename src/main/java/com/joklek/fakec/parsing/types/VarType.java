package com.joklek.fakec.parsing.types;

public enum VarType {
    INT, CHAR, STRING, BOOL, FLOAT;
    public DataType toDatatype() {
        return DataType.valueOf(this.toString());
    }
}
