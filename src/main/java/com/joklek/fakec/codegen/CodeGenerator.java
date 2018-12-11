package com.joklek.fakec.codegen;

import com.joklek.fakec.parsing.ast.*;
import com.joklek.fakec.parsing.types.data.DataType;
import com.joklek.fakec.parsing.types.element.ElementType;
import com.joklek.fakec.parsing.types.operation.OperationType;
import com.joklek.fakec.tokens.Token;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;
import java.util.ArrayList;
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
        this.interRepresentation = new IntermediateRepresentation(resolver);
    }

    public IntermediateRepresentation generate(Stmt.Program program) {
        visitProgramStmt(program);
        /*for (Label label : interRepresentation.getLabels()) {
            interRepresentation.placeLabel(label);
        }*/

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
        // TODO place main entry label here pls

        Label fnLabel = functionStmt.getLabel();
        interRepresentation.placeLabel(fnLabel);

        for (Pair<Token, DataType> param : functionStmt.getParams()) {
            int pointer = ((StackDeclaredNode) functionStmt.getBody().getScope().resolve(param.getLeft(), ElementType.VARIABLE)).getStackSlot();
            interRepresentation.write(PEEK, pointer);
        }

        functionStmt.getBody().accept(this);
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
            if (returnStmt.getValue() != null) {
                returnStmt.getValue().accept(this);
            }
            else {
                throw new IllegalStateException("Return says that it has value, but it doesn't"); // TODO this should hit when returning null, must fix
            }
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

        Label endLabel = interRepresentation.newLabel();
        // TODO optimisation idea: If condition is true or false, should skip thingies
        for (Pair<IExpr, Stmt.Block> branch : ifStmt.getBranches()) {
            Label label = interRepresentation.newLabel();
            branch.getLeft().accept(this);

            interRepresentation.write(BZ, label);
            branch.getRight().accept(this);
            interRepresentation.write(BR, endLabel); // TODO is it really BR not JMP

            interRepresentation.placeLabel(label);
        }
        if(ifStmt.getElseBranch() != null) {
            ifStmt.getElseBranch().accept(this);
        }
        interRepresentation.placeLabel(endLabel);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While whileStmt) {
        Label startLabel = interRepresentation.newLabelAtCurrent();
        whileStmt.setStartLabel(startLabel);
        whileStmt.getCondition().accept(this);
        Label endLabel = interRepresentation.newLabel();
        whileStmt.setEndLabel(endLabel);
        interRepresentation.write(BZ, endLabel);
        whileStmt.getBody().accept(this);
        interRepresentation.write(BR, startLabel);
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
            StackDeclaredNode variableNode = (StackDeclaredNode) inputStmt.getScope().resolve(variable, ElementType.VARIABLE);
            interRepresentation.write(IN, ((NodeWithLabel)variableNode).getLabel());
            interRepresentation.write(POKE, variableNode.getStackSlot());
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var varStmt) {
        if (varStmt.getInitializer() != null) {
            varStmt.getInitializer().accept(this);
            interRepresentation.write(POKE, varStmt.getStackSlot());
        }
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
        interRepresentation.write(BR, label);
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue continueStmt) {
        Label label = continueStmt.getTarget().getStartLabel();
        interRepresentation.write(BR, label);
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
                interRepresentation.write(LT);
                break;
            case LESS_EQUAL:
                break;
            case GREATER:
                interRepresentation.write(GT);
                break;
            case GREATER_EQUAL:
                break;
            case EQUAL:
                interRepresentation.write(EQ);
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
                interRepresentation.write(AND);
                break;
            case OR:
                interRepresentation.write(OR);
                break;
            default:
                throw new UnsupportedOperationException("Incorrect operator type for operation" + operatorType);
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
        List<Integer> bytes = new ArrayList<>();

        Object value = literalExpr.getValue();
        if(value instanceof Integer) {
            bytes.add((Integer) value);
        }
        else if(value instanceof String) {
            for (char c : ((String) value).toCharArray()) {
                bytes.add((int) c);
            }
        }
        else if(value instanceof Character) {
            bytes.add((int) (Character) value);
        }
        else if(value instanceof Double) {
            byte[] doubleBytes = new byte[8];
            ByteBuffer.wrap(doubleBytes).putDouble((Double) value);
            for (byte doubleByte : doubleBytes) {
                bytes.add((int) doubleByte);
            }
        }
        else if(value instanceof Boolean) {
            bytes.add(((boolean) value) ? 1 : 0);
        }
        for (Integer aByte : bytes) {
            interRepresentation.write(PUSH, aByte);
        }

        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary unaryExpr) {
        // TODO
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable variableExpr) {
        // TODO
        int pointer = ((StackDeclaredNode) variableExpr.getScope().resolve(variableExpr.getName(), ElementType.VARIABLE)).getStackSlot();
        interRepresentation.write(PEEK, pointer);

        /*interRepresentation.write(PEEK, ERROR.getValue());*/

        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign assignExpr) {
        assignExpr.getValue().accept(this);
        int pointer = ((StackDeclaredNode)assignExpr.getScope().resolve(assignExpr.getName(), ElementType.VARIABLE)).getStackSlot();
        interRepresentation.write(POKE, pointer);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call callExpr) {
        Label label = ((NodeWithLabel)callExpr.getScope().resolve(callExpr.getIdent(), ElementType.FUNCTION)).getLabel();
        interRepresentation.write(PUSH, label);
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
