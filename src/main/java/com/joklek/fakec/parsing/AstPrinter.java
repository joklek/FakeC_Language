package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.Stmt;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
    public String print(Expr expr) {
        return expr.accept(this);
    }
    public String print(Stmt stmt) {
        return stmt.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.getOperator().getLexeme(), expr.getLeft(), expr.getRight());
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.getExpression());
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.getValue() == null) {
            return "null";
        }
        return expr.getValue().toString();
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.getName().getLexeme();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.getOperator().getLexeme(), expr.getLeft(), expr.getRight());
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.getOperator().getLexeme(), expr.getRight());
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize(expr.getName() + " = ", expr.getValue());
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return null;
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return null;
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        return null;
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return null;
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return null;
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        return parenthesize(stmt.getType() + " " + stmt.getName().getLexeme() + " = ", stmt.getInitializer());
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        return null;
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}
