package com.joklek.fakec.codegen;

public enum InstructionType {
    ADD(1, 0), SUB(2, 0), MUL(3, 0), DIV(4, 0),
    CALL(5, 1), RET(6, 0), RET_V(7, 0),
    POP(8, 0), PUSH(9, 1), PEEK(10, 1), POKE(12, 1),
    ERROR(11, 0), BR(13, 1);

    private final int value;
    private final int ops;

    InstructionType(int value, int numberOfOperators) {
        this.value = value;
        this.ops = numberOfOperators;
    }

    public int getValue() {
        return value;
    }
    public int getOps() {
        return ops;
    }
}
