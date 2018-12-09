package com.joklek.fakec.codegen;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.types.operation.OperationType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.joklek.fakec.codegen.InstructionType.*;

// 4.4
public class CodeGenerator implements Stmt.Visitor<Void>, Expr.Visitor<Void>  {

    private IntermediateRepresentation interRepresentation;
    private InstructionResolver resolver;

    public CodeGenerator() {
        this(new InstructionResolver());
    }

    public CodeGenerator(InstructionResolver resolver) {
        this.resolver = resolver;
        this.interRepresentation = new IntermediateRepresentation();
    }

    public IntermediateRepresentation generate(Stmt.Program program) {
        visitProgramStmt(program);
        return interRepresentation;
    }

    @Override
    public Void visitProgramStmt(Stmt.Program program) {
        for (Stmt.Function function : program.getFunctions()) {
            function.accept(this);
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function functionStmt) {
        functionStmt.getBody().accept(this);
        Label fnLabel = functionStmt.getLabel();

        interRepresentation.placeLabel(fnLabel);
        for (Integer offset : fnLabel.getOffsets()) {
            interRepresentation.replace(offset, fnLabel.getValue());
        }
        interRepresentation.write(RET); // always does return even if one was before
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block blockStmt) {
        for (Stmt statement : blockStmt.getStatements()) {
            statement.accept(this);
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return returnStmt) {
        if(returnStmt.getHasValue()) {
            returnStmt.getValue().accept(this);
            interRepresentation.write(RET_V);
        }
        else {
            interRepresentation.write(RET);
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression expressionStmt) {
        // TODO: Optimisation to skip literal expression statements?
        Expr expression = expressionStmt.getExpression();
        expression.accept(this);
        interRepresentation.write(POP);

        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If ifStmt) {

        // TODO optimisation idea: If condition is true or false, should skip thingies
        for (Pair<Expr, Stmt.Block> branch : ifStmt.getBranches()) {
            branch.getLeft().accept(this);
            Label label = interRepresentation.newLabel();

            interRepresentation.write(label.getValue());
            branch.getRight().accept(this);
            interRepresentation.placeLabel(label);
        }
        if(ifStmt.getElseBranch() != null) {
            ifStmt.getElseBranch().accept(this);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While whileStmt) {
        return null;
    }

    @Override
    public Void visitOutputStmt(Stmt.Output outputStmt) {
        return null;
    }

    @Override
    public Void visitInputStmt(Stmt.Input inputStmt) {
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var varStmt) {
        return null;
    }

    @Override
    public Void visitArrayStmt(Stmt.Array arrayStmt) {
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break breakStmt) {
        Label label = breakStmt.getLabel();
        interRepresentation.write(BR, label.getValue());
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue continueStmt) {
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary binaryExpr) {
        binaryExpr.getLeft().accept(this);
        binaryExpr.getRight().accept(this);
        OperationType operatorType = binaryExpr.getOperator().getType();
        // TODO map this stuff
        switch (operatorType) {
            case MULT:
                interRepresentation.write(ADD);
                break;
            case DIV:
                interRepresentation.write(DIV);
                break;
            case ADD:
                interRepresentation.write(ADD);
                break;
            case SUB:
                interRepresentation.write(SUB);
                break;
            case MOD:
                // TODO ???
                break;
            case NOT:
                break;
            case LESS:
                break;
            case LESS_EQUAL:
                break;
            case GREATER:
                break;
            case GREATER_EQUAL:
                break;
            case EQUAL:
                break;
            case EQUAL_EQUAL:
                break;
            case NOT_EQUAL:
                break;
            case PLUS_EQUAL:
                break;
            case MINUS_EQUAL:
                break;
            case MUL_EQUAL:
                break;
            case DIV_EQUAL:
                break;
            case MOD_EQUAL:
                break;
            case INC_PRE:
                break;
            case INC_POST:
                break;
            case DEC_PRE:
                break;
            case DEC_POST:
                break;
            case AND:
                break;
            case OR:
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping groupingExpr) {
        groupingExpr.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal literalExpr) {
        interRepresentation.write(PUSH, (int) literalExpr.getValue());
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary unaryExpr) {
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable variableExpr) {
        Label label = variableExpr.getTarget().getLabel();
        if(label != null) {
            interRepresentation.write(PEEK, label.getValue());
        }
        else {
            interRepresentation.write(PEEK, ERROR.getValue());
        }
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign assignExpr) {
        assignExpr.getValue().accept(this);
        Label label = assignExpr.getTarget().getLabel();
        interRepresentation.write(POKE, label.getValue());
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call callExpr) {
        interRepresentation.write(PUSH, callExpr.getTarget().getLabel().getValue());
        List<Expr> arguments = callExpr.getArguments();
        for (Expr argument : arguments) {
            argument.accept(this);
        }
        interRepresentation.write(CALL, arguments.size());
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(Expr.ArrayAccess arrayAccessExpr) {
        return null;
    }

    @Override
    public Void visitArrayCreateExpr(Expr.ArrayCreate arrayCreateExpr) {
        return null;
    }
}
