package com.joklek.fakec.parsing.types.data;

public enum VarType {
    INT, CHAR, STRING, BOOL, FLOAT;

    public DataType toDatatype() {
        return DataType.valueOf(this.toString());
    }
}
