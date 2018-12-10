package com.joklek.fakec.scope;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.IExpr;
import com.joklek.fakec.parsing.ast.IStmt;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.scope.error.TypeError;
import com.joklek.fakec.parsing.types.data.DataType;
import com.joklek.fakec.tokens.Token;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

// 4.3
/**
 * Scope checker checks the program scope for correct usage of return, continue, break
 */
public class ScopeChecker implements Stmt.VisitorWithErrors<Void, TypeError> {

    /**
     * Checks scope for incorrect loop or function usage
     * @param stmt root program
     * @return list of collected errors
     */
    public List<TypeError> checkScope(Stmt.Program stmt) {
        List<TypeError> errors = new ArrayList<>();
        visitProgramStmt(stmt, errors);
        return errors;
    }

    @Override
    public Void visitProgramStmt(Stmt.Program stmt, List<TypeError> errors) {
        List<Stmt.Function> functions = stmt.getFunctions();
        boolean hasMain = functions.stream().anyMatch(function -> function.getType() == DataType.INT
                                                   && function.getName().getLexeme().equals("main"));
        // TODO this will not work with multiple files, fix later
        if(!hasMain) {
            errors.add(new TypeError("Program must contain a function 'int main()' ", 0));
        }

        for (Stmt.Function function : functions) {
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
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt, List<TypeError> errors) {
        for (IStmt statement : stmt.getStatements()) {
            statement.accept(this, errors);
        }

        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt, List<TypeError> errors) {
        IExpr returnValue = stmt.getValue();
        IStmt parent = stmt.getParent();


        while(parent != null && !(parent instanceof Stmt.Function)) {
            parent = parent.getParent();
        }
        if(parent == null) {
            DataType dataType = DataType.NULL;
            errors.add(new TypeError("Return statement has to be in a function", dataType, stmt.getKeyword().getLine()));
            return null;
        }

        Stmt.Function parentFunction = (Stmt.Function) parent;
        DataType functionType = parentFunction.getType();

        if(functionType == DataType.VOID) {
            if(stmt.hasValue()) {
                errors.add(new TypeError("Return must not have a value with void function", stmt.getKeyword().getLine()));
            }
        }
        else {
            if (!stmt.hasValue()) {
                errors.add(new TypeError("Must return a value with the type of the function", functionType, stmt.getKeyword().getLine()));
            }
            else if (functionType != returnValue.getType()) {
                errors.add(new TypeError("Return statement returns a value not with the type of the function", functionType, returnValue.getType(), stmt.getKeyword().getLine()));
            }
        }

        stmt.setTarget(parentFunction);
        parentFunction.getReturnStmts().add(stmt);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt, List<TypeError> errors) {
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt, List<TypeError> errors) {
        for (Pair<IExpr, Stmt.Block> branch : stmt.getBranches()) {
            branch.getRight().accept(this, errors);
        }
        if(stmt.getElseBranch() != null) {
            stmt.getElseBranch().accept(this, errors);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt, List<TypeError> errors) {
        stmt.getBody().accept(this, errors);
        return null;
    }

    @Override
    public Void visitOutputStmt(Stmt.Output stmt, List<TypeError> errors) {
        return null;
    }

    @Override
    public Void visitInputStmt(Stmt.Input stmt, List<TypeError> errors) {
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt, List<TypeError> errors) {
        return null;
    }

    @Override
    public Void visitArrayStmt(Stmt.Array stmt, List<TypeError> errors) {
        return null;
    }

    // TODO
    @Override
    public Void visitBreakStmt(Stmt.Break stmt, List<TypeError> errors) {
        IStmt parent = stmt.getParent();
        return resolveParentWhile(parent, errors, stmt.getToken());
    }

    // TODO
    @Override
    public Void visitContinueStmt(Stmt.Continue stmt, List<TypeError> errors) {
        IStmt parent = stmt.getParent();
        return resolveParentWhile(parent, errors, stmt.getToken());
    }

    private Void resolveParentWhile(IStmt parent, List<TypeError> errors, Token token) {
        while (parent != null && !(parent instanceof Stmt.While)) {
            parent = parent.getParent();
        }
        if (parent == null) {
            errors.add(new TypeError(String.format("%s should be inside of while or for", token.getType()), token.getLine()));
        }
        return null;
    }
}
