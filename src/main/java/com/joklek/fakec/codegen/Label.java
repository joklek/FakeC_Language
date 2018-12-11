package com.joklek.fakec.codegen;

import java.util.ArrayList;
import java.util.List;

public class Label {

    private int value;
    private final List<Integer> offsets;

    public Label() {
        this.offsets = new ArrayList<>();
    }

    public Label(int value) {
        this.value = value;
        this.offsets = new ArrayList<>();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public List<Integer> getOffsets() {
        return offsets;
    }

    public void addOffset(int offset) {
        offsets.add(offset);
    }
}
