package com.joklek.fakec.parsing.ast;

import com.joklek.fakec.error.Error;
import com.joklek.fakec.parsing.types.Node;
import com.joklek.fakec.scope.Scope;

import java.util.List;

public interface IStmt extends Node {
    interface Visitor<R> {
        R visitProgramStmt(Stmt.Program programStmt);
        R visitFunctionStmt(Stmt.Function functionStmt);
        R visitBlockStmt(Stmt.Block blockStmt);
        R visitReturnStmt(Stmt.Return returnStmt);
        R visitExpressionStmt(Stmt.Expression expressionStmt);
        R visitIfStmt(Stmt.If ifStmt);
        R visitWhileStmt(Stmt.While whileStmt);
        R visitOutputStmt(Stmt.Output outputStmt);
        R visitInputStmt(Stmt.Input inputStmt);
        R visitVarStmt(Stmt.Var varStmt);
        R visitArrayStmt(Stmt.Array arrayStmt);
        R visitBreakStmt(Stmt.Break breakStmt);
        R visitContinueStmt(Stmt.Continue continueStmt);
        R visitForStmt(Stmt.For forStmt);
}

    interface VisitorWithErrors<R, E extends Error> {
        R visitProgramStmt(Stmt.Program programStmt, List<E> errors);
        R visitFunctionStmt(Stmt.Function functionStmt, List<E> errors);
        R visitBlockStmt(Stmt.Block blockStmt, List<E> errors);
        R visitReturnStmt(Stmt.Return returnStmt, List<E> errors);
        R visitExpressionStmt(Stmt.Expression expressionStmt, List<E> errors);
        R visitIfStmt(Stmt.If ifStmt, List<E> errors);
        R visitWhileStmt(Stmt.While whileStmt, List<E> errors);
        R visitOutputStmt(Stmt.Output outputStmt, List<E> errors);
        R visitInputStmt(Stmt.Input inputStmt, List<E> errors);
        R visitVarStmt(Stmt.Var varStmt, List<E> errors);
        R visitArrayStmt(Stmt.Array arrayStmt, List<E> errors);
        R visitBreakStmt(Stmt.Break breakStmt, List<E> errors);
        R visitContinueStmt(Stmt.Continue continueStmt, List<E> errors);
        R visitForStmt(Stmt.For forStmt, List<E> errors);
    }

    Scope getScope();
    void setScope(Scope scope);

    IStmt getParent();
    void setParent(IStmt parent);

    <R> R accept(IStmt.Visitor<R> visitor);
    <R, E extends Error> R accept(IStmt.VisitorWithErrors<R, E> visitor, List<E> errors);
}
