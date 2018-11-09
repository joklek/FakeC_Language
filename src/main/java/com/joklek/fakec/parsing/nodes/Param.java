package com.joklek.fakec.parsing.nodes;

public class Param implements Node {

    private final String name;
    private final Type type;

    public Param(String name, Type type) {
        this.name = name;
        this.type = type;
    }
}
