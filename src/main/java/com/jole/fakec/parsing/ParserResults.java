package com.jole.fakec.parsing;

import com.jole.fakec.parsing.error.ParserError;
import com.jole.fakec.parsing.nodes.Node;

import java.util.List;

public class ParserResults {
    private final Node rootNode;
    private final List<ParserError> errors;

    public ParserResults(Node rootNode, List<ParserError> errors) {
        this.rootNode = rootNode;
        this.errors = errors;
    }

    public Node getRootNode() {
        return rootNode;
    }

    public List<ParserError> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
