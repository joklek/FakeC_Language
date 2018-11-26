package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.error.ScopeError;
import com.joklek.fakec.parsing.types.DataType;
import com.joklek.fakec.tokens.Token;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ScopeResolver implements Expr.Visitor<List<ScopeError>>, Stmt.Visitor<List<ScopeError>> {

    public List<ScopeError> resolveNames(Stmt.Program stmt) {
        return stmt.accept(this);
    }

    @Override
    public List<ScopeError> visitProgramStmt(Stmt.Program stmt) {
        List<ScopeError> errors = new ArrayList<>();
        stmt.setScope(new Scope());
        Scope scope = stmt.getScope();
        for(Stmt.Function function: stmt.getFunctions()) {
            ScopeError error = scope.add(function.getName().getLexeme(), function.getType());
            if(error != null) {
                errors.add(error);
            }
        }
        for(Stmt.Function function: stmt.getFunctions()) {
            function.setScope(scope);
            List<ScopeError> deepErrors = function.accept(this);
            errors.addAll(deepErrors);
        }
        return errors;
    }

    @Override
    public List<ScopeError> visitFunctionStmt(Stmt.Function stmt) {
        List<ScopeError> errors = new ArrayList<>();

        Scope parentScope = stmt.getScope();
        Scope scope = new Scope(parentScope);
        for(Pair<Token, DataType> pair: stmt.getParams()) {
            ScopeError error = scope.add(pair.getLeft().getLexeme(), pair.getRight());
            if(error != null) {
                errors.add(error);
            }
        }
        stmt.getBody().setScope(scope);

        List<ScopeError> deepErrors = stmt.getBody().accept(this);
        errors.addAll(deepErrors);

        return errors;
    }

    @Override
    public List<ScopeError> visitBlockStmt(Stmt.Block stmt) {
        List<ScopeError> errors = new ArrayList<>();

        Scope scope = stmt.getScope();
        for(Stmt statement: stmt.getStatements()) {
            statement.setScope(scope);
            List<ScopeError> deepErrors = statement.accept(this);
            errors.addAll(deepErrors);
        }
        return errors;
    }

    @Override
    public List<ScopeError> visitReturnStmt(Stmt.Return stmt) {
        return null;
    }

    @Override
    public List<ScopeError> visitExpressionStmt(Stmt.Expression stmt) {
        return null;
    }

    @Override
    public List<ScopeError> visitIfStmt(Stmt.If stmt) {
        return null;
    }

    @Override
    public List<ScopeError> visitWhileStmt(Stmt.While stmt) {
        return null;
    }

    @Override
    public List<ScopeError> visitOutputStmt(Stmt.Output stmt) {
        return null;
    }

    @Override
    public List<ScopeError> visitInputStmt(Stmt.Input stmt) {
        return null;
    }

    @Override
    public List<ScopeError> visitVarStmt(Stmt.Var stmt) {
        List<ScopeError> errors = new ArrayList<>();

        ScopeError error = stmt.getScope().add(stmt.getName().getLexeme(), stmt.getType());
        if(error != null) {
            errors.add(error);
        }

        return errors;
    }

    @Override
    public List<ScopeError> visitArrayStmt(Stmt.Array stmt) {
        return null;
    }

    @Override
    public List<ScopeError> visitBreakStmt(Stmt.Break stmt) {
        return null;
    }

    @Override
    public List<ScopeError> visitContinueStmt(Stmt.Continue stmt) {
        return null;
    }

    @Override
    public List<ScopeError> visitBinaryExpr(Expr.Binary expr) {
        return null;
    }

    @Override
    public List<ScopeError> visitGroupingExpr(Expr.Grouping expr) {
        return null;
    }

    @Override
    public List<ScopeError> visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public List<ScopeError> visitUnaryExpr(Expr.Unary expr) {
        return null;
    }

    @Override
    public List<ScopeError> visitVariableExpr(Expr.Variable expr) {
        return null;
    }

    @Override
    public List<ScopeError> visitAssignExpr(Expr.Assign expr) {
        return null;
    }

    @Override
    public List<ScopeError> visitCallExpr(Expr.Call expr) {
        return null;
    }

    @Override
    public List<ScopeError> visitArrayAccessExpr(Expr.ArrayAccess expr) {
        return null;
    }

    @Override
    public List<ScopeError> visitArrayCreateExpr(Expr.ArrayCreate expr) {
        return null;
    }
}
