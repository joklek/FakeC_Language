package com.joklek.fakec.parsing.types.operation;

public class OperatorToken {
    private final OperationType type;
    private final int line;

    public OperatorToken(OperationType type, int line) {
        this.type = type;
        this.line = line;
    }

    public OperationType getType() {
        return type;
    }

    public int getLine() {
        return line;
    }
}
