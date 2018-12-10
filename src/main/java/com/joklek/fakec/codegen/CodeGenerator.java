package com.joklek.fakec.codegen;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.IExpr;
import com.joklek.fakec.parsing.ast.IStmt;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.types.operation.OperationType;
import com.joklek.fakec.tokens.Token;
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
        for (IStmt statement : blockStmt.getStatements()) {
            statement.accept(this);
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return returnStmt) {
        if(returnStmt.hasValue()) {
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
        IExpr expression = expressionStmt.getExpression();
        expression.accept(this);
        interRepresentation.write(POP);

        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If ifStmt) {

        // TODO optimisation idea: If condition is true or false, should skip thingies
        for (Pair<IExpr, Stmt.Block> branch : ifStmt.getBranches()) {
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
        Label startLabel = interRepresentation.newLabelAtCurrent();
        //whileStmt.setStartLabel(startLabel);
        whileStmt.getCondition().accept(this);
        Label endLabel = interRepresentation.newLabel();
        //whileStmt.setEndLabel(endLabel);
        interRepresentation.write(BZ, endLabel.getValue());
        whileStmt.getBody().accept(this);
        interRepresentation.write(BR, startLabel.getValue());
        interRepresentation.placeLabel(endLabel);
        return null;
    }

    @Override
    public Void visitOutputStmt(Stmt.Output outputStmt) {
        for (IExpr expression : outputStmt.getExpressions()) {
            expression.accept(this);
            interRepresentation.write(OUT);
        }
        return null;
    }

    @Override
    public Void visitInputStmt(Stmt.Input inputStmt) {
        for (Token variable : inputStmt.getVariables()) {
            //interRepresentation.write(IN, variable);
            // TODO
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var varStmt) {
        // TODO
        return null;
    }

    @Override
    public Void visitArrayStmt(Stmt.Array arrayStmt) {
        // TODO
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break breakStmt) {
        Label label = breakStmt.getTarget().getEndLabel();
        interRepresentation.write(BR, label.getValue());
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue continueStmt) {
        Label label = continueStmt.getTarget().getStartLabel();
        interRepresentation.write(BR, label.getValue());
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
        // TODO
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable variableExpr) {
       /* Label label = variableExpr.getTarget().getLabel();
        if(label != null) {
            interRepresentation.write(PEEK, label.getValue());
        }
        else {
            interRepresentation.write(PEEK, ERROR.getValue());
        }*/
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign assignExpr) {
        /*assignExpr.getValue().accept(this);
        Label label = assignExpr.getTarget().getLabel();
        interRepresentation.write(POKE, label.getValue());*/
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
        // TODO
        return null;
    }

    @Override
    public Void visitArrayCreateExpr(Expr.ArrayCreate arrayCreateExpr) {
        // TODO
        return null;
    }
}
