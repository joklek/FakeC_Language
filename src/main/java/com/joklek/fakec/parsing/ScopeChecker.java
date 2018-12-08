package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.error.TypeError;
import com.joklek.fakec.parsing.types.data.DataType;
import com.joklek.fakec.tokens.Token;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ScopeChecker implements Expr.VisitorWithErrors<Void, TypeError>, Stmt.VisitorWithErrors<Void, TypeError> {
    @Override
    public Void visitBinaryExpr(Expr.Binary expr, List<TypeError> errors) {
        Expr left = expr.getLeft();
        Expr right = expr.getRight();

        // TODO Refactor
        if(left.getType() == DataType.VOID) {
            DataType voidType = left.getType();
            errors.add(new TypeError(String.format("No operations are possible with void type function '%s'(...)", ((Expr.Call) left).getIdent().getLexeme()), voidType.getLine()));
            return null;
        }
        if(right.getType() == DataType.VOID) {
            DataType voidType = right.getType();
            errors.add(new TypeError(String.format("No operations are possible with void type function '%s'(...)", ((Expr.Call) right).getIdent().getLexeme()), voidType.getLine()));
            return null;
        }
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr, List<TypeError> errors) {
        expr.getExpression().accept(this, errors);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr, List<TypeError> errors) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr, List<TypeError> errors) {
        expr.getRight().accept(this, errors);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr, List<TypeError> errors) {
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr, List<TypeError> errors) {
        expr.getValue().accept(this, errors);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr, List<TypeError> errors) {
        for (Expr argument : expr.getArguments()) {
            argument.accept(this, errors);
        }

        return null;
    }

    @Override
    public Void visitArrayAccessExpr(Expr.ArrayAccess expr, List<TypeError> errors) {
        expr.getOffset().accept(this, errors);
        return null;
    }

    @Override
    public Void visitArrayCreateExpr(Expr.ArrayCreate expr, List<TypeError> errors) {
        expr.getSize().accept(this, errors);
        return null;
    }

    @Override
    public Void visitProgramStmt(Stmt.Program stmt, List<TypeError> errors) {
        for (Stmt.Function function : stmt.getFunctions()) {
            function.accept(this, errors);
        }

        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt, List<TypeError> errors) {
        stmt.getBody().accept(this, errors);
        boolean isVoid = stmt.getType() == DataType.VOID;
        if(stmt.getReturnStmts().isEmpty() && !isVoid) {
            errors.add(new TypeError(String.format("Function '%s' has no return statements", stmt.getName().getLexeme()), stmt.getName().getLine()));
        }
        for (Stmt.Return returnStmt : stmt.getReturnStmts()) {
            if(!returnStmt.getHasValue() && !isVoid) {
                errors.add(new TypeError(String.format("Return in function '%s' must have a value", stmt.getName().getLexeme()), stmt.getType()));
            }
            else {
                // TODO null should not be null, but some kind of special token
                Expr returnValue = returnStmt.getValue();
                if(returnValue == null) {
                    // TODO decide when null can be returned
                }
                else if (returnValue.getType() != stmt.getType()) {
                    errors.add(new TypeError(String.format("Incorrect return value in function '%s'", stmt.getName().getLexeme()), stmt.getType(), returnValue.getType()));
                }
            }
        }

        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt, List<TypeError> errors) {
        for (Stmt statement : stmt.getStatements()) {
            statement.accept(this, errors);
        }

        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt, List<TypeError> errors) {
        Expr returnValue = stmt.getValue();
        Stmt parent = stmt.getParent();


        while(parent != null && !(parent instanceof Stmt.Function)) {
            parent = parent.getParent();
        }
        if(parent == null) {
            DataType dataType = DataType.NULL;
            dataType.setLine(stmt.getKeyword().getLine());
            errors.add(new TypeError("Return statement has to be in a function", dataType));
            return null;
        }

        Stmt.Function parentFunction = (Stmt.Function) parent;
        DataType functionType = parentFunction.getType();

        if (!stmt.getHasValue()) {
            if(functionType != DataType.VOID) {
                errors.add(new TypeError("Must return a value with the type of the function", functionType, DataType.VOID));
            }
            parentFunction.getReturnStmts().add(stmt);
            return null;
        }

        if (functionType != returnValue.getType()) {
            errors.add(new TypeError("Return statement returns a value not with the type of the function", functionType, returnValue.getType()));
        }
        else {
            parentFunction.getReturnStmts().add(stmt);
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt, List<TypeError> errors) {
        stmt.getExpression().accept(this, errors);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt, List<TypeError> errors) {
        for (Pair<Expr, Stmt.Block> branch : stmt.getBranches()) {
            branch.getLeft().accept(this, errors);
            branch.getRight().accept(this, errors);
        }
        stmt.getElseBranch().accept(this, errors);

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt, List<TypeError> errors) {
        stmt.getCondition().accept(this, errors);
        stmt.getBody().accept(this, errors);
        return null;
    }

    @Override
    public Void visitOutputStmt(Stmt.Output stmt, List<TypeError> errors) {
        for (Expr expression : stmt.getExpressions()) {
            expression.accept(this, errors);
        }

        return null;
    }

    @Override
    public Void visitInputStmt(Stmt.Input stmt, List<TypeError> errors) {
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt, List<TypeError> errors) {
        stmt.getInitializer().accept(this, errors);
        return null;
    }

    @Override
    public Void visitArrayStmt(Stmt.Array stmt, List<TypeError> errors) {
        stmt.getInitializer().accept(this, errors);
        return null;
    }

    // TODO
    @Override
    public Void visitBreakStmt(Stmt.Break stmt, List<TypeError> errors) {
        Stmt parent = stmt.getParent();
        return resolveParentWhile(parent, errors, stmt.getToken());
    }

    // TODO
    @Override
    public Void visitContinueStmt(Stmt.Continue stmt, List<TypeError> errors) {
        Stmt parent = stmt.getParent();
        return resolveParentWhile(parent, errors, stmt.getToken());
    }

    private Void resolveParentWhile(Stmt parent, List<TypeError> errors, Token token) {
        while (parent != null && !(parent instanceof Stmt.While)) {
            parent = parent.getParent();
        }
        if (parent == null) {
            errors.add(new TypeError(String.format("%s should be inside of while or for", token.getType()), token.getLine()));
        }
        return null;
    }
}
