package com.joklek.fakec.parsing.ast;

public interface StackDeclaredNode extends NodeWithType {
    int getStackSlot();
    void setStackSlot(int slot);
}
