package com.joklek.fakec.parsing.ast;

import com.joklek.fakec.codegen.Label;
import com.joklek.fakec.parsing.types.Node;

public interface NodeWithLabel extends Node {
    Label getLabel();
}
