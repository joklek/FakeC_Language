package com.joklek.fakec.parsing.ast;

import com.joklek.fakec.codegen.Label;
import com.joklek.fakec.parsing.types.Node;

public interface LoopNode extends Node {
    Label getEndLabel();
    void setEndLabel(Label label);
    Label getStartLabel();
    void setStartLabel(Label label);
}
