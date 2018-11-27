package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.error.ScopeError;
import com.joklek.fakec.parsing.types.DataType;
import com.joklek.fakec.tokens.Token;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ScopeResolver implements Expr.Visitor<List<ScopeError>>, Stmt.Visitor<List<ScopeError>> {

    public List<ScopeError> resolveNames(Stmt.Program stmt, Scope scope) {
        stmt.setScope(scope);
        return stmt.accept(this);
    }

    @Override
    public List<ScopeError> visitProgramStmt(Stmt.Program stmt) {
        List<ScopeError> errors = new ArrayList<>();
        Scope scope = stmt.getScope();
        for(Stmt.Function function: stmt.getFunctions()) {
            ScopeError error = scope.add(function.getName(), function.getType());
            if(error != null) {
                errors.add(error);
            }
        }
        for(Stmt.Function function: stmt.getFunctions()) {
            List<ScopeError> deepErrors = setScopeAndSearchForErrors(scope, function);
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
            ScopeError error = scope.add(pair.getLeft(), pair.getRight());
            if(error != null) {
                errors.add(error);
            }
        }
        List<ScopeError> deepErrors = setScopeAndSearchForErrors(scope, stmt.getBody());
        errors.addAll(deepErrors);

        return errors;
    }

    @Override
    public List<ScopeError> visitBlockStmt(Stmt.Block stmt) {
        List<ScopeError> errors = new ArrayList<>();

        Scope scope = new Scope(stmt.getScope());
        for(Stmt statement: stmt.getStatements()) {
            List<ScopeError> deepErrors = setScopeAndSearchForErrors(scope, statement);
            errors.addAll(deepErrors);
        }
        return errors;
    }

    @Override
    public List<ScopeError> visitReturnStmt(Stmt.Return stmt) {
        Expr returnValue = stmt.getValue();
        if(returnValue != null) {
            returnValue.setScope(stmt.getScope());
            return returnValue.accept(this);
        }
        return Collections.emptyList();
    }

    @Override
    public List<ScopeError> visitExpressionStmt(Stmt.Expression stmt) {
        Expr expression = stmt.getExpression();
        expression.setScope(stmt.getScope());
        return expression.accept(this);
    }

    @Override
    public List<ScopeError> visitIfStmt(Stmt.If stmt) {
        List<ScopeError> errors = new ArrayList<>();
        Scope scope = stmt.getScope();
        List<ScopeError> deepErrors;

        for (Pair<Expr, Stmt.Block> branch : stmt.getBranches()) {
            deepErrors = setScopeAndSearchForErrors(scope, branch.getLeft());
            errors.addAll(deepErrors);

            deepErrors = setScopeAndSearchForErrors(scope, branch.getRight());
            errors.addAll(deepErrors);
        }

        // TODO remove duplicate code
        Stmt.Block elseBranch = stmt.getElseBranch();
        if(elseBranch != null) {
            deepErrors = setScopeAndSearchForErrors(scope, elseBranch);
            errors.addAll(deepErrors);
        }

        return errors;
    }

    @Override
    public List<ScopeError> visitWhileStmt(Stmt.While stmt) {
        List<ScopeError> errors = new ArrayList<>();
        Scope scope = stmt.getScope();
        List<ScopeError> deepErrors;

        deepErrors = setScopeAndSearchForErrors(scope, stmt.getCondition());
        errors.addAll(deepErrors);

        deepErrors = setScopeAndSearchForErrors(scope, stmt.getBody());
        errors.addAll(deepErrors);

        return errors;
    }

    @Override
    public List<ScopeError> visitOutputStmt(Stmt.Output stmt) {
        List<ScopeError> errors = new ArrayList<>();
        Scope scope = stmt.getScope();
        for (Expr expression : stmt.getExpressions()) {
            List<ScopeError> deepErrors = setScopeAndSearchForErrors(scope, expression);
            errors.addAll(deepErrors);
        }
        return errors;
    }

    @Override
    public List<ScopeError> visitInputStmt(Stmt.Input stmt) {
        List<ScopeError> errors = new ArrayList<>();
        Scope scope = stmt.getScope();
        for (Token variable : stmt.getVariables()) {
            getResolveError(scope, variable)
                    .ifPresent(error -> errors.add(error));
        }
        return errors;
    }

    @Override
    public List<ScopeError> visitVarStmt(Stmt.Var stmt) {
        List<ScopeError> errors = new ArrayList<>();

        // Analyze initializer before variable initialization, so it could not be initialized as itself
        Expr initializer = stmt.getInitializer();
        Scope scope = stmt.getScope();
        if(initializer != null) {
            List<ScopeError> deepErrors = setScopeAndSearchForErrors(scope, initializer);
            errors.addAll(deepErrors);
        }

        ScopeError error = scope.add(stmt.getName(), stmt.getType());
        if(error != null) {
            errors.add(error);
        }

        return errors;
    }

    @Override
    public List<ScopeError> visitBreakStmt(Stmt.Break stmt) {
        return Collections.emptyList();
    }

    @Override
    public List<ScopeError> visitContinueStmt(Stmt.Continue stmt) {
        return Collections.emptyList();
    }

    @Override
    public List<ScopeError> visitBinaryExpr(Expr.Binary expr) {
        List<ScopeError> errors = new ArrayList<>();

        Scope scope = expr.getScope();
        Expr rightExpr = expr.getRight();
        rightExpr.setScope(scope);
        Expr leftExpr = expr.getLeft();
        leftExpr.setScope(scope);

        errors.addAll(leftExpr.accept(this));
        errors.addAll(rightExpr.accept(this));

        return errors;
    }

    @Override
    public List<ScopeError> visitGroupingExpr(Expr.Grouping expr) {
        Scope scope = expr.getScope();
        Expr expression = expr.getExpression();
        expression.setScope(scope);
        return expression.accept(this);
    }

    @Override
    public List<ScopeError> visitLiteralExpr(Expr.Literal expr) {
        return Collections.emptyList();
    }

    @Override
    public List<ScopeError> visitUnaryExpr(Expr.Unary expr) {
        Scope scope = expr.getScope();
        Expr rightExpr = expr.getRight();
        rightExpr.setScope(scope);
        return rightExpr.accept(this);
    }

    @Override
    public List<ScopeError> visitVariableExpr(Expr.Variable expr) {
        List<ScopeError> errors = new ArrayList<>();
        getResolveError(expr.getScope(), expr.getName())
                .ifPresent(error -> errors.add(error));

        return errors;
    }

    @Override
    public List<ScopeError> visitAssignExpr(Expr.Assign expr) {
        List<ScopeError> errors = new ArrayList<>();
        Scope scope = expr.getScope();

        getResolveError(scope, expr.getName())
                .ifPresent(error -> errors.add(error));

        expr.getValue().setScope(scope);
        errors.addAll(expr.getValue().accept(this));

        return errors;
    }

    @Override
    public List<ScopeError> visitCallExpr(Expr.Call expr) {
        List<ScopeError> errors = new ArrayList<>();
        Scope scope = expr.getScope();

        getResolveError(scope, expr.getIdent())
                .ifPresent(error -> errors.add(error));

        for (Expr argument : expr.getArguments()) {
            List<ScopeError> deepErrors = setScopeAndSearchForErrors(scope, argument);
            errors.addAll(deepErrors);
        }
        return errors;
    }

    @Override
    public List<ScopeError> visitArrayStmt(Stmt.Array stmt) {
        List<ScopeError> errors = new ArrayList<>();
        Scope scope = stmt.getScope();

        ScopeError error = scope.add(stmt.getName(), stmt.getType());
        if(error != null) {
            errors.add(error);
        }


        Expr initializer = stmt.getInitializer();
        if(initializer != null) {
            List<ScopeError> deepErrors = setScopeAndSearchForErrors(scope, initializer);
            errors.addAll(deepErrors);
        }

        getResolveError(scope, stmt.getName())
                .ifPresent(resolveError -> errors.add(resolveError));
        return errors;
    }

    @Override
    public List<ScopeError> visitArrayAccessExpr(Expr.ArrayAccess expr) {
        Scope scope = expr.getScope();

        List<ScopeError> deepErrors = setScopeAndSearchForErrors(scope, expr.getOffset());
        List<ScopeError> errors = new ArrayList<>(deepErrors);

        getResolveError(scope, expr.getArray())
                .ifPresent(error -> errors.add(error));
        return errors;
    }

    @Override
    public List<ScopeError> visitArrayCreateExpr(Expr.ArrayCreate expr) {
        Scope scope = expr.getScope();

        List<ScopeError> deepErrors = setScopeAndSearchForErrors(scope, expr.getSize());
        List<ScopeError> errors = new ArrayList<>(deepErrors);

        getResolveError(scope, expr.getArray())
                .ifPresent(error -> errors.add(error));
        return errors;
    }

    private List<ScopeError> setScopeAndSearchForErrors(Scope scope, Expr expression) {
        expression.setScope(scope);
        return expression.accept(this);
    }

    private List<ScopeError> setScopeAndSearchForErrors(Scope scope, Stmt statement) {
        statement.setScope(scope);
        return statement.accept(this);
    }

    private Optional<ScopeError> getResolveError(Scope scope, Token token) {
        try {
            scope.resolve(token);
        } catch (ScopeError e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }
}
