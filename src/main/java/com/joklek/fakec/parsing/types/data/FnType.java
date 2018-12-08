package com.joklek.fakec.parsing.types.data;

public enum FnType {
    INT, CHAR, STRING, BOOL, FLOAT, VOID;

    public DataType toDatatype() {
        return DataType.valueOf(this.toString());
    }
}
