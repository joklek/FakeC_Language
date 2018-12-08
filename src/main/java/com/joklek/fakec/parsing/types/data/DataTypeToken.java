package com.joklek.fakec.parsing.types.data;

public class DataTypeToken {
    private final DataType type;
    private final int line;

    public DataTypeToken(DataType type, int line) {
        this.type = type;
        this.line = line;
    }

    public DataType getType() {
        return type;
    }

    public int getLine() {
        return line;
    }
}
