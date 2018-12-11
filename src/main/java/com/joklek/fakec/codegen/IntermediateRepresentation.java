package com.joklek.fakec.codegen;

import java.util.ArrayList;
import java.util.List;

public class IntermediateRepresentation {
    private final List<Label> labels;
    private final List<Integer> instructionBytes;
    private final InstructionResolver resolver;

    public IntermediateRepresentation() {
        this(new InstructionResolver());
    }

    public IntermediateRepresentation(InstructionResolver resolver) {
        this.instructionBytes = new ArrayList<>();
        this.labels = new ArrayList<>();
        this.resolver = resolver;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public Label newLabel() {
        Label label = new Label();
        labels.add(label);
        return label;
    }

    public List<Integer> getInstructionBytes() {
        return instructionBytes;
    }

    public void placeLabel(Label label) {
        label.setValue(instructionBytes.size());
        for (Integer offset : label.getOffsets()) {
            instructionBytes.set(offset, label.getValue());
        }
    }

    public void write(InstructionType instruction, Label label) {
        labels.add(label);
        write(instruction.getValue(), label.getValue());
        label.addOffset(instructionBytes.size() - 1);
    }

    public void write(InstructionType instruction, int... params) {
        write(instruction.getValue(), params);
    }

    public void write(int instruction, int... params) {
        instructionBytes.add(instruction);

        InstructionType instructionType = resolver.resolveInstruction(instruction);
        if(instructionType.getOps() != params.length) {
            throw new IllegalArgumentException(String.format("Writing %s. Expected %d params but got %d.", instructionType, instructionType.getOps(), params.length));
        }

        for (int param : params) {
            instructionBytes.add(param);
        }
    }

    public Label newLabelAtCurrent() {
        return new Label(instructionBytes.size());
    }
}
