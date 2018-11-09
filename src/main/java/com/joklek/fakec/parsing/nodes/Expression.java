package com.joklek.fakec.parsing.nodes;

import com.joklek.fakec.tokens.TokenType;

public interface Expression extends Node {
    class ExprConst implements Expression {

    }

    class ExprBinary implements Expression {

        private final TokenType type;
        private final Expression left;
        private final Expression right;

        public ExprBinary(TokenType type, Expression left, Expression right) {
            this.type = type;
            this.left = left;
            this.right = right;
        }

        public TokenType getType() {
            return type;
        }

        public Expression getLeft() {
            return left;
        }

        public Expression getRight() {
            return right;
        }
    }

    class ExprPrio implements Expression {

    }

    class ExprVar implements Expression {

    }
}
