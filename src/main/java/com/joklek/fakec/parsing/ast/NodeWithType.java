package com.joklek.fakec.parsing.ast;

import com.joklek.fakec.parsing.types.Node;
import com.joklek.fakec.parsing.types.data.DataType;

public interface NodeWithType extends Node {
    DataType getType();
}
