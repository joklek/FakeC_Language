package com.joklek.fakec.scope;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.IExpr;
import com.joklek.fakec.parsing.ast.IStmt;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.scope.error.ScopeError;
import com.joklek.fakec.scope.error.TypeError;
import com.joklek.fakec.parsing.types.operation.OperatorToken;
import com.joklek.fakec.parsing.types.data.DataType;
import com.joklek.fakec.parsing.types.operation.OperationType;
import com.joklek.fakec.tokens.Token;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.joklek.fakec.parsing.types.element.ElementType.FUNCTION;
import static com.joklek.fakec.parsing.types.element.ElementType.VARIABLE;
import static com.joklek.fakec.parsing.types.operation.OperationType.*;

// 4.2
/**
 * Checks the types of operations
 */
@SuppressWarnings("squid:S3516")
public class TypeChecker implements Expr.VisitorWithErrors<Void, TypeError>, Stmt.VisitorWithErrors<Void, TypeError> {

    /**
     * Checks the given program and returns all type errors like incompatible types,
     * @param stmt root program
     * @return type errors for given program
     */
    public List<TypeError> checkForTypeErrors(Stmt.Program stmt) {
        List<TypeError> errors = new ArrayList<>();
        visitProgramStmt(stmt, errors);
        return errors;
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
        return stmt.getBody().accept(this, errors);
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
        if (stmt.getValue() != null) {
            stmt.getValue().accept(this, errors);
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

        for (Pair<IExpr, Stmt.Block> branch : stmt.getBranches()) {
            branch.getLeft().accept(this, errors);
            branch.getRight().accept(this, errors);

            DataType type = branch.getLeft().getType();
            if(type != DataType.BOOL) {
                errors.add(new TypeError("Condition should be of boolean type", DataType.BOOL, type, -1)); // TODO
            }
        }
        if(stmt.getElseBranch() != null) {
            stmt.getElseBranch().accept(this, errors);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt, List<TypeError> errors) {

        stmt.getBody().accept(this, errors);
        stmt.getCondition().accept(this, errors);

        if (stmt.getCondition().getType() != DataType.BOOL) {
            errors.add(new TypeError("Condition should be of boolean type", DataType.BOOL, stmt.getCondition().getType(), -1)); // TODO
        }

        return null;
    }

    @Override
    public Void visitOutputStmt(Stmt.Output stmt, List<TypeError> errors) {
        for (IExpr expression : stmt.getExpressions()) {
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
        IExpr initializer = stmt.getInitializer();
        if (initializer == null) {
            return null;
        }

        initializer.accept(this, errors);
        if(stmt.getType() != initializer.getType()) {
            errors.add(new TypeError(String.format("Can't assign to variable %s",  stmt.getName().getLexeme()), stmt.getType(), initializer.getType(), stmt.getName().getLine()));
        }

        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt, List<TypeError> errors) {
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt, List<TypeError> errors) {
        return null;
    }


    @Override
    public Void visitBinaryExpr(Expr.Binary expr, List<TypeError> errors) {

        Expr left = expr.getLeft();
        Expr right = expr.getRight();
        left.accept(this, errors);
        right.accept(this, errors);
        DataType leftType = left.getType();
        DataType rightType = right.getType();

        // TODO REFACTOR
        if(leftType == DataType.VOID) {
            Token fnCall = ((Expr.Call) left).getIdent();
            errors.add(new TypeError(String.format("No operations are possible with void type function '%s'(...)", fnCall.getLexeme()), fnCall.getLine()));
        }
        if(rightType == DataType.VOID) {
            Token fnCall = ((Expr.Call) right).getIdent();
            errors.add(new TypeError(String.format("No operations are possible with void type function '%s'(...)", fnCall.getLexeme()), fnCall.getLine()));
        }

        OperatorToken operator = expr.getOperator();
        OperationType operatorType = operator.getType();

        if(operatorType == EQUAL_EQUAL || operatorType == NOT_EQUAL) {
            expr.setType(DataType.BOOL);
            return null;
        }

        // TODO refactor this clusterduck
        if(operatorType == LESS || operatorType == GREATER || operatorType == LESS_EQUAL || operatorType == GREATER_EQUAL) {
            if ((leftType != DataType.INT && leftType != DataType.FLOAT) || (rightType != DataType.INT && rightType != DataType.FLOAT)) {
                DataType badType = leftType == DataType.INT ? rightType : leftType == DataType.FLOAT ? rightType : leftType;
                errors.add(new TypeError(String.format("Operation %s only possible with Integers and float", operatorType), badType, operator.getLine()));
            }
            expr.setType(DataType.BOOL);
            return null;
        }
        if(operatorType == OR || operatorType == AND ) {
            expr.setType(DataType.BOOL);
            return null;
        }

        // TODO Revise logic
        if(operatorType == ADD || operatorType == SUB || operatorType == MULT || operatorType == DIV) {
            if(leftType == DataType.FLOAT && rightType == DataType.FLOAT ||
               leftType == DataType.INT && rightType == DataType.FLOAT ||
               leftType == DataType.FLOAT && rightType == DataType.INT) {
                expr.setType(DataType.FLOAT);
            }
            else if (leftType == DataType.INT && rightType == DataType.INT) {
                expr.setType(DataType.INT);
            }
            else {
                DataType badType = leftType == DataType.INT ? rightType : leftType == DataType.FLOAT ? rightType : leftType;
                errors.add(new TypeError("Arithmetic operations is only possible on integers or floats", badType, operator.getLine()));
                expr.setType(leftType); // Not sure if this is correct, but want to avoid errors
            }
        }
        else if(operatorType == MOD) {
            if(leftType == DataType.INT && rightType == DataType.INT) {
                expr.setType(DataType.INT);
            }
            else {
                errors.add(new TypeError("Modulus operations is only possible on integers", leftType, rightType, operator.getLine()));
            }
        }
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr, List<TypeError> errors) {
        expr.getExpression().accept(this, errors);
        expr.setType(expr.getExpression().getType());
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr, List<TypeError> errors) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr, List<TypeError> errors) {
        expr.getRight().accept(this, errors);
        DataType type = expr.getRight().getType();
        OperatorToken operator = expr.getOperator();
        OperationType operatorType = operator.getType();
        expr.setType(type);
        if( ((type == DataType.FLOAT || type == DataType.INT) && (operatorType == ADD || operatorType == SUB)) // Float or int for + or -
                || ((operatorType == INC_POST || operatorType == INC_PRE || operatorType == DEC_POST || operatorType == DEC_PRE) && type == DataType.INT) // int for --/++ pre and post
                || (type == DataType.BOOL && operatorType == NOT)) {  // !bool
            return null;
        }
        errors.add(new TypeError(String.format("Unary operation %s cannot be used on type %s", operatorType, type), type, operator.getLine()));
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr, List<TypeError> errors) {
        DataType type;
        try {
            type = expr.getScope().resolve(expr.getName(), VARIABLE).getType();
        }
        catch (ScopeError e) {
            return null;
        }
        expr.setType(type);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr, List<TypeError> errors) {

        DataType type;
        try {
            type = expr.getScope().resolve(expr.getName(), VARIABLE).getType();
        }
        catch (ScopeError e) {
            return null;
        }

        expr.getValue().accept(this, errors);
        DataType valueType = expr.getValue().getType();
        if(type != valueType) {
            if(type == DataType.INT && valueType == DataType.FLOAT ) {
                expr.setType(DataType.INT);
            }
            else {
                errors.add(new TypeError(String.format("Can't assign variable %s", expr.getName().getLexeme()), valueType, type, expr.getName().getLine()));
            }
        }
        else {
            expr.setType(expr.getValue().getType());
        }
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr, List<TypeError> errors) {

        try {
            Stmt.Function function = (Stmt.Function) expr.getScope().resolve(expr.getIdent(), FUNCTION);
            DataType functionType = function.getType();
            expr.setType(functionType);

            for (Expr argument : expr.getArguments()) {
                argument.accept(this, errors);
            }
        }
        catch (ScopeError e) {
            return null;
        }
        return null;
    }

    @Override
    public Void visitArrayStmt(Stmt.Array stmt, List<TypeError> errors) {
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(Expr.ArrayAccess expr, List<TypeError> errors) {
        return null;
    }

    @Override
    public Void visitArrayCreateExpr(Expr.ArrayCreate expr, List<TypeError> errors) {
        return null;
    }
}
