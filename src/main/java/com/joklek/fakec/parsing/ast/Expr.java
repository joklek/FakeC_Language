package com.joklek.fakec.parsing.ast;

import com.joklek.fakec.error.Error;
import com.joklek.fakec.parsing.types.data.DataType;
import com.joklek.fakec.parsing.types.operation.OperatorToken;
import com.joklek.fakec.scope.Scope;
import com.joklek.fakec.tokens.Token;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Expr implements IExpr {

    private Scope scope = null;
    @Override
    public Scope getScope() {
        return scope;
    }
    @Override
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    private DataType type = null;
    @Override
    public DataType getType() {
        return type;
    }
    @Override
    public void setType(DataType type) {
        this.type = type;
    }

    public static class Binary extends Expr {

        private final Expr left;
        private final OperatorToken operator;
        private final Expr right;

        public Binary(Expr left, OperatorToken operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public Expr getLeft() {
            return left;
        }

        public OperatorToken getOperator() {
            return operator;
        }

        public Expr getRight() {
            return right;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitBinaryExpr(this, errors);
        }
    }

    public static class Grouping extends Expr {

        private final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        public Expr getExpression() {
            return expression;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitGroupingExpr(this, errors);
        }
    }

    public static class Literal extends Expr {

        private final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitLiteralExpr(this, errors);
        }
    }

    public static class Unary extends Expr {

        private final OperatorToken operator;
        private final Expr right;

        public Unary(OperatorToken operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        public OperatorToken getOperator() {
            return operator;
        }

        public Expr getRight() {
            return right;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitUnaryExpr(this, errors);
        }
    }

    public static class Variable extends Expr {

        private final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        public Token getName() {
            return name;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitVariableExpr(this, errors);
        }
    }

    public static class Assign extends Expr {

        private final Token name;
        private final Expr value;
        private final Expr offset;
        
        public Assign(Token name, Expr value) {
            this(name, value, null);
        }

        public Assign(Token name, Expr value, @Nullable Expr offset) {
            this.name = name;
            this.value = value;
            this.offset = offset;
        }

        public Token getName() {
            return name;
        }

        public Expr getValue() {
            return value;
        }

        @Nullable
        public Expr getOffset() {
            return offset;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitAssignExpr(this, errors);
        }
    }

    public static class Call extends Expr {

        private final Token ident;
        private final List<Expr> arguments;

        public Call(Token ident, List<Expr> arguments) {
            this.ident = ident;
            this.arguments = arguments;
        }

        public Token getIdent() {
            return ident;
        }

        public List<Expr> getArguments() {
            return arguments;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitCallExpr(this, errors);
        }
    }

    public static class ArrayAccess extends Expr {

        private final Token array;
        private final Expr offset;

        public ArrayAccess(Token array, Expr offset) {
            this.array = array;
            this.offset = offset;
        }

        public Token getArray() {
            return array;
        }

        public Expr getOffset() {
            return offset;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayAccessExpr(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitArrayAccessExpr(this, errors);
        }
    }

    /*public static class ArrayCreate extends Expr {

        private final Token name;
        private final int size;
        private final DataType arrayType;

        public ArrayCreate(Token name, DataType arrayType, int size) {
            this.name = name;
            this.size = size;
            this.arrayType = arrayType;
        }

        public Token getName() {
            return name;
        }

        public int getSize() {
            return size;
        }

        public DataType getArrayType() {
            return arrayType;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayCreateExpr(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitArrayCreateExpr(this, errors);
        }
    }*/

    public static class Random extends Expr {

        private final Expr minInclusive;
        private final Expr maxInclusive;
        private final Token token;

        public Random(Expr minInclusive, Expr maxInclusive, Token token) {
            this.maxInclusive = maxInclusive;
            this.minInclusive = minInclusive;
            this.token = token;
        }

        public Expr getMinInclusive() {
            return minInclusive;
        }

        public Expr getMaxInclusive() {
            return maxInclusive;
        }

        public Token getToken() {
            return token;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitRandom(this);
        }

        @Override
        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitRandom(this, errors);
        }
    }
}
