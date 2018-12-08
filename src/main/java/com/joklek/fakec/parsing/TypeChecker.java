package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.error.ScopeError;
import com.joklek.fakec.parsing.error.TypeError;
import com.joklek.fakec.parsing.types.operation.OperatorToken;
import com.joklek.fakec.parsing.types.data.DataType;
import com.joklek.fakec.parsing.types.operation.OperationType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.joklek.fakec.parsing.types.element.ElementType.FUNCTION;
import static com.joklek.fakec.parsing.types.element.ElementType.VARIABLE;
import static com.joklek.fakec.parsing.types.operation.OperationType.*;

@SuppressWarnings("squid:S3516")
public class TypeChecker implements Expr.VisitorWithErrors<Void, TypeError>, Stmt.VisitorWithErrors<Void, TypeError> {

    @Override
    public Void visitProgramStmt(Stmt.Program stmt, List<TypeError> errors) {
        for (Stmt.Function function : stmt.getFunctions()) {
            function.getBody().accept(this, errors);
        }

        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt, List<TypeError> errors) {
        return stmt.getBody().accept(this, errors);
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
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt, List<TypeError> errors) {
        return stmt.getExpression().accept(this, errors);
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt, List<TypeError> errors) {

        for (Pair<Expr, Stmt.Block> branch : stmt.getBranches()) {
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
        Expr initializer = stmt.getInitializer();
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

        OperatorToken operator = expr.getOperator();
        OperationType operatorType = operator.getType();
        if(operatorType == LESS || operatorType == GREATER || operatorType == LESS_EQUAL || operatorType == GREATER_EQUAL ||
                operatorType == EQUAL_EQUAL || operatorType == NOT_EQUAL) {
            expr.setType(DataType.BOOL);
            return null;
        }

        DataType leftType = left.getType();
        DataType rightType = right.getType();

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
                errors.add(new TypeError("Arithmetic operations is only possible on integers or floats", leftType, rightType, operator.getLine()));
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
        if(((type == DataType.FLOAT || type == DataType.INT) && (operatorType == ADD || operatorType == SUB))
                || type == DataType.BOOL && operatorType == NOT) {
            return null;
        }
        errors.add(new TypeError(String.format("Unary operation %s cannot be used on type %s", operatorType, type), type, operator.getLine()));
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr, List<TypeError> errors) {
        DataType type;
        try {
            type = expr.getScope().resolve(expr.getName(), VARIABLE);
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
            type = expr.getScope().resolve(expr.getName(), VARIABLE);
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

        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr, List<TypeError> errors) {

        try {
            DataType functionType = expr.getScope().resolve(expr.getIdent(), FUNCTION);
            expr.setType(functionType);
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
