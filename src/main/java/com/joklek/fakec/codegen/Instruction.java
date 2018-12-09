package com.joklek.fakec.codegen;

public class Instruction {
    private final InstructionType instructionType;
    private Label label;

    public Instruction(InstructionType instructionType, Label label) {
        this.instructionType = instructionType;
        this.label = label;
    }

    public Instruction(InstructionType instructionType) {
        this.instructionType = instructionType;
        this.label = label;
    }

    public InstructionType getInstructionType() {
        return instructionType;
    }

    public Label getLabel() {
        return label;
    }
}
