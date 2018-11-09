package com.joklek.fakec.parsing.nodes;

import java.util.List;

public interface Statement extends Node {
    class ReturnStmt implements Statement {
        private final Node value;

        public ReturnStmt() {
            this.value = null;
        }

        public ReturnStmt(Node value) {
            this.value = value;
        }

        public Node getValue() {
            return value;
        }
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

    class InputStmt implements Statement {
        private final List<String> identifiers;
        public InputStmt(List<String> identifiers) {
            this.identifiers = identifiers;
        }

        public List<String> getIdentifiers() {
            return identifiers;
        }
    }

    class OutputStmt implements Statement {

        private final List<Expression> expressions;
        public OutputStmt(List<Expression> expressions) {
            this.expressions = expressions;
        }

        public List<Expression> getExpressions() {
            return expressions;
        }
    }
}
