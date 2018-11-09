package com.joklek.fakec.parsing.nodes;

import java.util.ArrayList;
import java.util.List;

public class Program implements Node {

    private final List<Node> nodes;

    public Program() {
        nodes = new ArrayList<>();
    }

    public void add_func(DefFunc func) {
        nodes.add(func);
    }

    public List<Node> getNodes() {
        return nodes;
    }
}
