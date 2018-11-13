package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.error.ParserError;

import java.util.List;

public class ParserResults {
    private final Stmt.Program rootNode;
    private final List<ParserError> errors;

    public ParserResults(Stmt.Program rootNode, List<ParserError> errors) {
        this.rootNode = rootNode;
        this.errors = errors;
    }

    public Stmt.Program getRootNode() {
        return rootNode;
    }

    public List<ParserError> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
