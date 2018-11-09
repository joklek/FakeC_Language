package com.joklek.fakec.parsing.nodes;

import java.util.ArrayList;
import java.util.List;

public class StmtBlock implements Node {

    private final List<Statement> statements;

    public StmtBlock() {
        this.statements = new ArrayList<>();
    }

    public void addStmt(Statement stmt) {
        this.statements.add(stmt);
    }

    public List<Statement> getStatements() {
        return statements;
    }
}
