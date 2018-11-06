package com.jole.fakec.parsing.nodes;

import java.util.List;

public class DefFunc implements Node {

    private final String name;
    private final List<Param> parameters;
    private final Type returnType;
    private final StmtBlock body;

    public DefFunc(Type returnType, String name, List<Param> parameters , StmtBlock body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<Param> getParameters() {
        return parameters;
    }

    public Type getReturnType() {
        return returnType;
    }

    public StmtBlock getBody() {
        return body;
    }
}
