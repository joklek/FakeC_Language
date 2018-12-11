package com.joklek.fakec.scope;

public class StackSlotPointer {
    private int currentStackSlot;

    public StackSlotPointer() {
        this.currentStackSlot = 0;
    }

    public int getCurrentStackSlot() {
        return currentStackSlot;
    }

    public void incCurrentStackSlot() {
        currentStackSlot++;
    }

    public void resetCurrentStackSlot() {
        currentStackSlot = 0;
    }
}
