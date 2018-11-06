package com.jole.fakec.parsing.nodes;

import java.util.List;

public interface Statement extends Node {
    class ReturnStmt implements Statement {
    }

    class BreakStmt implements Statement {
    }

    class ContinueStmt implements Statement {
    }

    class Declaration implements Statement {
        private final Type type;
        private final String name;
        private final Expression value;

        public Declaration(Type type, String name, Expression value) {
            this.type = type;
            this.name = name;
            this.value = value;
        }

        public Type getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public Expression getValue() {
            return value;
        }
    }

    class Declarations implements Statement {

        private final List<Declaration> declarations;
        private final Type type;

        public Declarations(Type type, List<Declaration> declarations) {
            this.declarations = declarations;
            this.type = type;
        }

        public List<Declaration> getDeclarations() {
            return declarations;
        }

        public Type getType() {
            return type;
        }
    }

    class ExpressionStatement implements Statement {

        private final Expression expression;
        public ExpressionStatement(Expression expression) {
            this.expression = expression;
        }

        public Expression getExpression() {
            return expression;
        }
    }
}
