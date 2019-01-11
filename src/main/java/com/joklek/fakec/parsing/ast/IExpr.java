package com.joklek.fakec.parsing.ast;

import com.joklek.fakec.error.Error;
import com.joklek.fakec.parsing.types.data.DataType;
import com.joklek.fakec.scope.Scope;

import java.util.List;

public interface IExpr extends NodeWithMutableType {
    interface Visitor<R> {
        R visitBinaryExpr(Expr.Binary binaryExpr);
        R visitGroupingExpr(Expr.Grouping groupingExpr);
        R visitLiteralExpr(Expr.Literal literalExpr);
        R visitUnaryExpr(Expr.Unary unaryExpr);
        R visitVariableExpr(Expr.Variable variableExpr);
        R visitAssignExpr(Expr.Assign assignExpr);
        R visitCallExpr(Expr.Call callExpr);
        R visitArrayAccessExpr(Expr.ArrayAccess arrayAccessExpr);
        R visitRandom(Expr.Random random);
    }

    interface VisitorWithErrors<R, E extends Error> {
        R visitBinaryExpr(Expr.Binary binaryExpr, List<E> errors);
        R visitGroupingExpr(Expr.Grouping groupingExpr, List<E> errors);
        R visitLiteralExpr(Expr.Literal literalExpr, List<E> errors);
        R visitUnaryExpr(Expr.Unary unaryExpr, List<E> errors);
        R visitVariableExpr(Expr.Variable variableExpr, List<E> errors);
        R visitAssignExpr(Expr.Assign assignExpr, List<E> errors);
        R visitCallExpr(Expr.Call callExpr, List<E> errors);
        R visitArrayAccessExpr(Expr.ArrayAccess arrayAccessExpr, List<E> errors);
        R visitRandom(Expr.Random random, List<E> errors);
    }

    Scope getScope();
    void setScope(Scope scope);

    DataType getType();
    void setType(DataType type);

    <R> R accept(Expr.Visitor<R> visitor);
    <R, E extends Error> R accept(Expr.VisitorWithErrors<R, E> visitor, List<E> errors);
}
