package com.joklek.fakec.scope;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.IExpr;
import com.joklek.fakec.parsing.ast.IStmt;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.scope.error.ScopeError;
import com.joklek.fakec.parsing.types.data.DataType;
import com.joklek.fakec.parsing.types.element.ElementType;
import com.joklek.fakec.tokens.Token;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.joklek.fakec.parsing.types.element.ElementType.*;

// 4.1
/**
 * Scope resolver builds the scope for a given AST and gets all scope related errors like name clashing or absence of names
 */
@SuppressWarnings({"Convert2MethodRef", "squid:S1612"})
public class ScopeResolver implements Expr.VisitorWithErrors<Void, ScopeError>, Stmt.VisitorWithErrors<Void, ScopeError> {

    /**
     * Resolves name scopes and gets errors
     * @param stmt root program statement
     * @param scope parent scope
     * @return list of collected scope errors
     */
    public List<ScopeError> resolveNames(Stmt.Program stmt, Scope scope) {
        List<ScopeError> errors = new ArrayList<>();
        stmt.setScope(scope);
        stmt.accept(this, errors);
        return errors;
    }

    @Override
    public Void visitProgramStmt(Stmt.Program stmt, List<ScopeError> errors) {
        Scope scope = stmt.getScope();
        for(Stmt.Function function: stmt.getFunctions()) {
            ScopeError error = scope.add(function.getName(), function, FUNCTION);
            if(error != null) {
                errors.add(error);
            }
            function.setParent(stmt);
        }
        for(Stmt.Function function: stmt.getFunctions()) {
            setScopeAndSearchForErrors(scope, function, errors);
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt, List<ScopeError> errors) {

        Scope parentScope = stmt.getScope();
        Scope scope = new Scope(parentScope);
        scope.getPointer().resetCurrentStackSlot();
        for(Pair<Token, DataType> pair: stmt.getParams()) {
            // Will treat parameter as a variable, probably not the best idea, but I'm running out of time. I'd rather write witty comments than fix mistakes of my own design
            ScopeError error = scope.add(pair.getLeft(), new Stmt.Var(pair.getRight(), pair.getLeft(), null), VARIABLE);
            if(error != null) {
                errors.add(error);
            }
        }
        setScopeAndSearchForErrors(scope, stmt.getBody(), errors);
        stmt.getBody().setParent(stmt);

        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt, List<ScopeError> errors) {

        Scope scope = new Scope(stmt.getScope());
        for(IStmt statement: stmt.getStatements()) {
            setScopeAndSearchForErrors(scope, statement, errors);
            statement.setParent(stmt);
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt, List<ScopeError> errors) {
        IExpr returnValue = stmt.getValue();
        if(returnValue != null) {
            returnValue.setScope(stmt.getScope());
            returnValue.accept(this, errors);
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt, List<ScopeError> errors) {
        IExpr expression = stmt.getExpression();
        expression.setScope(stmt.getScope());
        expression.accept(this, errors);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt, List<ScopeError> errors) {
        Scope scope = stmt.getScope();

        for (Pair<IExpr, Stmt.Block> branch : stmt.getBranches()) {
            setScopeAndSearchForErrors(scope, branch.getLeft(), errors);
            setScopeAndSearchForErrors(scope, branch.getRight(), errors);
            branch.getRight().setParent(stmt);
        }

        Stmt.Block elseBranch = stmt.getElseBranch();
        if(elseBranch != null) {
            setScopeAndSearchForErrors(scope, elseBranch, errors);
            elseBranch.setParent(stmt);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt, List<ScopeError> errors) {
        Scope scope = stmt.getScope();
        setScopeAndSearchForErrors(scope, stmt.getCondition(), errors);
        setScopeAndSearchForErrors(scope, stmt.getBody(), errors);

        stmt.getBody().setParent(stmt);
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For forStmt, List<ScopeError> errors) {
        Scope scope = forStmt.getScope();

        for (IStmt iStmt : forStmt.getInitializer()) {
            setScopeAndSearchForErrors(scope, iStmt, errors);
        }

        setScopeAndSearchForErrors(scope, forStmt.getCondition(), errors);
        setScopeAndSearchForErrors(scope, forStmt.getBody(), errors);

        Expr inc = forStmt.getIncrement();
        if(inc != null) {
            setScopeAndSearchForErrors(scope, inc, errors);
        }
        forStmt.getBody().setParent(forStmt);
        return null;
    }

    @Override
    public Void visitOutputStmt(Stmt.Output stmt, List<ScopeError> errors) {
        Scope scope = stmt.getScope();
        for (IExpr expression : stmt.getExpressions()) {
            setScopeAndSearchForErrors(scope, expression, errors);
        }
        return null;
    }

    @Override
    public Void visitInputStmt(Stmt.Input stmt, List<ScopeError> errors) {
        Scope scope = stmt.getScope();
        for (Token variable : stmt.getVariables()) {
            getResolveError(scope, variable, VARIABLE)
                    .ifPresent(error -> errors.add(error));
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt, List<ScopeError> errors) {
        // Analyze initializer before variable initialization, so it could not be initialized as itself
        IExpr initializer = stmt.getInitializer();
        Scope scope = stmt.getScope();
        if(initializer != null) {
            setScopeAndSearchForErrors(scope, initializer, errors);
        }

        ScopeError error = scope.add(stmt.getName(), stmt, VARIABLE);
        if(error != null) {
            errors.add(error);
        }
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt, List<ScopeError> errors) {
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt, List<ScopeError> errors) {
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr, List<ScopeError> errors) {

        Scope scope = expr.getScope();
        Expr rightExpr = expr.getRight();
        rightExpr.setScope(scope);
        Expr leftExpr = expr.getLeft();
        leftExpr.setScope(scope);

        leftExpr.accept(this, errors);
        rightExpr.accept(this, errors);

        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr, List<ScopeError> errors) {
        Scope scope = expr.getScope();
        Expr expression = expr.getExpression();
        expression.setScope(scope);
        expression.accept(this, errors);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr, List<ScopeError> errors) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr, List<ScopeError> errors) {
        Scope scope = expr.getScope();
        Expr rightExpr = expr.getRight();
        rightExpr.setScope(scope);
        rightExpr.accept(this, errors);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr, List<ScopeError> errors) {

        getResolveError(expr.getScope(), expr.getName(), VARIABLE)
                .ifPresent(error -> errors.add(error));
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr, List<ScopeError> errors) {
        Scope scope = expr.getScope();

        getResolveError(scope, expr.getName(), VARIABLE)
                .ifPresent(error -> errors.add(error));

        expr.getValue().setScope(scope);
        expr.getValue().accept(this, errors);

        if(expr.getOffset() != null) {
            expr.getOffset().accept(this, errors);
            expr.getOffset().setScope(scope);
        }

        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr, List<ScopeError> errors) {
        Scope scope = expr.getScope();

        getResolveError(scope, expr.getIdent(), FUNCTION)
                .ifPresent(error -> errors.add(error));

        for (Expr argument : expr.getArguments()) {
            setScopeAndSearchForErrors(scope, argument, errors);
        }

        return null;
    }

    @Override
    public Void visitArrayStmt(Stmt.Array stmt, List<ScopeError> errors) {
        Scope scope = stmt.getScope();

        ScopeError error = scope.add(stmt.getName(), stmt, VARIABLE);
        scope.getPointer().addSlots(stmt.getSize());
        if(error != null) {
            errors.add(error);
        }
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(Expr.ArrayAccess expr, List<ScopeError> errors) {
        Scope scope = expr.getScope();

        setScopeAndSearchForErrors(scope, expr.getOffset(), errors);
        getResolveError(scope, expr.getArray(), VARIABLE)
                .ifPresent(error -> errors.add(error));
        return null;
    }

    @Override
    public Void visitRandom(Expr.Random random, List<ScopeError> errors) {
        Scope scope = random.getScope();
        setScopeAndSearchForErrors(scope, random.getMinInclusive(), errors);
        setScopeAndSearchForErrors(scope, random.getMaxInclusive(), errors);
        return null;
    }

    private void setScopeAndSearchForErrors(Scope scope, IExpr expression, List<ScopeError> errors) {
        expression.setScope(scope);
        expression.accept(this, errors);
    }

    private void setScopeAndSearchForErrors(Scope scope, IStmt statement, List<ScopeError> errors) {
        statement.setScope(scope);
        statement.accept(this, errors);
    }

    private Optional<ScopeError> getResolveError(Scope scope, Token token, ElementType elementType) {
        try {
            scope.resolve(token, elementType);
        } catch (ScopeError e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }
}
