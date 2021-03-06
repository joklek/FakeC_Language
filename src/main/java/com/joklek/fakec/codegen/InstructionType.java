package com.joklek.fakec.codegen;

public enum InstructionType {
    ADDI(0x10, 0), ADDF(0x11, 0),
    SUBI(0x12, 0), SUBF(0x13, 0),
    MULI(0x14, 0), MULF(0x15, 0),
    DIVI(0x16, 0), DIVF(0x17, 0),
    MOD(0x18, 0),

    EQI(0x20, 0), EQF(0x21, 0),
    LTI(0x22, 0), LTF(0x23, 0),
    LEI(0x24, 0), LEF(0x25, 0),
    GTI(0x26, 0), GTF(0x27, 0),
    GEI(0x28, 0), GEF(0x29, 0),
    NEI(0x2A, 0), NEF(0x2B, 0),

    AND(0x30, 0),
    OR(0x31, 0),
    NOT(0x32, 0),


    // popas vienas, pushas tik int ir float
    POP(0x40, 0), POPF(0x41, 0),
    PUSHI(0x45, 1),PUSHF(0x46, 1),

    PEEK(0x50, 1), POKE(0x51, 1), ALLOC(0x52, 1),
    PEEKS(0x5A, 0), POKES(0x5B, 0),

    LEA(0x5C, 0), LD(0x5D, 0),

    CALL(0x60, 1), RET(0x61, 0), RET_V(0x62, 0), EXIT(0x63, 0),
    JMP(0x64, 1), JMPZ(0x65, 1),
    RND(0x66, 0),

    STDOI(0x70,0),
    STDOF(0x71,0),
    STDOS(0x72,0),
    STDOC(0x73,0),
    STDOB(0x74,0),

    STDINI(0x75,0),
    STDINF(0x76,0),
    STDINC(0x77, 0),
    STDINS(0x78, 0);

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
