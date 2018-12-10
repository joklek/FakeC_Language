package com.joklek.fakec.parsing.ast;

import com.joklek.fakec.parsing.types.data.DataType;

public interface NodeWithMutableType extends NodeWithType {
    void setType(DataType type);
}
