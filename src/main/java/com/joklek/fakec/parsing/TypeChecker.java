package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.error.TypeError;
import com.joklek.fakec.parsing.types.DataType;
import com.joklek.fakec.parsing.types.OperationType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.joklek.fakec.parsing.types.OperationType.*;

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
                errors.add(new TypeError(String.format("Condition should be of boolean type and is %s", type), null));
            }
        }
        stmt.getElseBranch().accept(this, errors);

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt, List<TypeError> errors) {

        stmt.getBody().accept(this, errors);
        stmt.getCondition().accept(this, errors);

        if (stmt.getCondition().getType() != DataType.BOOL) {
            errors.add(new TypeError(String.format("Condition should be of boolean type and is %s", stmt.getCondition().getType()), null));
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

        if(initializer != null) {
            initializer.accept(this, errors);

            if(stmt.getType() != initializer.getType() &&
                    stmt.getType() != DataType.INT || initializer.getType() != DataType.FLOAT) {
                errors.add(new TypeError(String.format("Can't assign %s to %s", initializer.getType(), stmt.getType()), stmt.getName()));
            }
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

        OperationType operator = expr.getOperator();
        if(operator == LESS || operator == GREATER || operator == LESS_EQUAL || operator == GREATER_EQUAL ||
                operator == EQUAL_EQUAL || operator == NOT_EQUAL) {
            expr.setType(DataType.BOOL);
        }
        else {
            DataType leftType = left.getType();
            DataType rightType = right.getType();
            if(operator == ADD || operator == SUB || operator == MULT || operator == DIV) {
                if(leftType == DataType.FLOAT && rightType == DataType.FLOAT ||
                   leftType == DataType.INT && rightType == DataType.FLOAT ||
                   leftType == DataType.FLOAT && rightType == DataType.INT) {
                    expr.setType(DataType.FLOAT);
                }
                else if (leftType == DataType.INT && rightType == DataType.INT) {
                    expr.setType(DataType.INT);
                }
                else {
                    // TODO token
                    errors.add(new TypeError(String.format("Arithmetic operations is only possible on integers or floats, but provided were %s and %s", leftType, rightType), null));
                }
            }
            else if(operator == MOD) {
                if(leftType == DataType.INT && rightType == DataType.INT) {
                    expr.setType(DataType.INT);
                }
                else {
                    // TODO token
                    errors.add(new TypeError(String.format("Modulus operations is only possible on integers, but provided were %s and %s", leftType, rightType), null));
                }
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
        OperationType operator = expr.getOperator();
        expr.setType(type);
        if((type == DataType.FLOAT || type == DataType.INT)
                && (operator == ADD || operator == SUB)) {
            return null;
        }
        else if(type == DataType.BOOL && operator == NOT) {
            return null;
        }
        // TODO Think of something here
        errors.add(new TypeError(String.format("Unary operation %s cannot be used on type %s", operator, type), null));
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr, List<TypeError> errors) {
        DataType type = expr.getScope().resolve(expr.getName());
        expr.setType(type);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr, List<TypeError> errors) {

        DataType type = expr.getScope().resolve(expr.getName());

        expr.getValue().accept(this, errors);
        DataType valueType = expr.getValue().getType();
        if(type != valueType) {
            if(type == DataType.INT && valueType == DataType.FLOAT ) {
                expr.setType(DataType.INT);
            }
            else {
                errors.add(new TypeError(String.format("Can't assign %s to %s", valueType, type), expr.getName()));
            }
        }

        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr, List<TypeError> errors) {

        try {
            // TODO should not get Type, but whole node which will have type and etc.
            expr.getScope().resolve(expr.getIdent());
        }
        catch (TypeError e) {
            errors.add(e);
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
