package com.joklek.fakec.codegen;

import com.joklek.fakec.parsing.ast.*;
import com.joklek.fakec.parsing.types.data.DataType;
import com.joklek.fakec.parsing.types.element.ElementType;
import com.joklek.fakec.parsing.types.operation.OperationType;
import com.joklek.fakec.parsing.types.operation.OperatorToken;
import com.joklek.fakec.scope.Scope;
import com.joklek.fakec.tokens.Token;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.joklek.fakec.codegen.InstructionType.*;

// 4.4
public class CodeGenerator implements Stmt.Visitor<Void>, Expr.Visitor<Void>  {

    private IntermediateRepresentation interRepresentation;
    private Label mainLabel;

    public CodeGenerator() {
        this(new InstructionResolver());
    }

    public CodeGenerator(InstructionResolver resolver) {
        this.interRepresentation = new IntermediateRepresentation(resolver, new StringTable());
    }

    public IntermediateRepresentation generate(Stmt.Program program) {
        visitProgramStmt(program);
        return interRepresentation;
    }

    @Override
    public Void visitProgramStmt(Stmt.Program program) {
        mainLabel = interRepresentation.newLabel();
        interRepresentation.write(PUSHI, mainLabel);
        interRepresentation.write(PUSHI, 666);
        interRepresentation.write(PUSHI, 666);
        interRepresentation.write(CALL, 0);
        interRepresentation.write(EXIT);
        for (Stmt.Function function : program.getFunctions()) {
            function.accept(this);
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function functionStmt) {
        if(functionStmt.getType() == DataType.INT && functionStmt.getName().getLexeme().equals("main") && functionStmt.getParams().isEmpty()) {
            interRepresentation.placeLabel(mainLabel);
        }

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
        switch (expression.getType()) {
            case INT:
                interRepresentation.write(POPI);
                break;
            case CHAR:
                interRepresentation.write(POPC);
                break;
            case STRING:
                interRepresentation.write(POPS);
                break;
            case BOOL:
                interRepresentation.write(POPB);
                break;
            case FLOAT:
                interRepresentation.write(POPF);
                break;
            case NULL:
                interRepresentation.write(POPB); // ODO null should be revised when pointers are implemented
                break;
            case VOID:
                break;
            default:
                throw new UnsupportedOperationException("Unsupported literal type " + expression.getType());
        }
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If ifStmt) {

        Label endLabel = interRepresentation.newLabel();
        // TODO optimisation idea: If condition is true or false, should skip thingies
        for (Pair<IExpr, Stmt.Block> branch : ifStmt.getBranches()) {
            IExpr condition = branch.getLeft();
            Stmt.Block block = branch.getRight();

            Label label = interRepresentation.newLabel();
            condition.accept(this);

            interRepresentation.write(JMPZ, label);
            block.accept(this);
            interRepresentation.write(JMP, endLabel);

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
        interRepresentation.write(JMPZ, endLabel);
        whileStmt.getBody().accept(this);
        interRepresentation.write(JMP, startLabel);
        interRepresentation.placeLabel(endLabel);
        return null;
    }

    @Override
    public Void visitOutputStmt(Stmt.Output outputStmt) {
        for (IExpr expression : outputStmt.getExpressions()) {
            expression.accept(this);
            switch (expression.getType()) {
                case INT:
                    interRepresentation.write(STDOI);
                    break;
                case CHAR:
                    interRepresentation.write(STDOC);
                    break;
                case STRING:
                    interRepresentation.write(STDOS);
                    break;
                case BOOL:
                    interRepresentation.write(STDOB);
                    break;
                case FLOAT:
                    interRepresentation.write(STDOF);
                    break;
                case NULL:
                    // TODO Pointers dude?
                    break;
                case VOID:
                    throw new UnsupportedOperationException("Output is not possible with void type functions");
                default:
                    throw new UnsupportedOperationException("Unsupported output statement with type '" + expression.getType() + "'");
            }
        }
        return null;
    }

    @Override
    public Void visitInputStmt(Stmt.Input inputStmt) {
        for (Token variable : inputStmt.getVariables()) {
            StackDeclaredNode variableNode = (StackDeclaredNode) inputStmt.getScope().resolve(variable, ElementType.VARIABLE);
            interRepresentation.write(STDIN, ((NodeWithLabel)variableNode).getLabel());
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
    public Void visitBreakStmt(Stmt.Break breakStmt) {
        Label label = breakStmt.getTarget().getEndLabel();
        interRepresentation.write(JMP, label);
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue continueStmt) {
        Label label = continueStmt.getTarget().getStartLabel();
        interRepresentation.write(JMP, label);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary binaryExpr) {

        // TODO: Fix bitwise operations
        binaryExpr.getLeft().accept(this);
        binaryExpr.getRight().accept(this);
        DataType type = binaryExpr.getType();
        OperationType operationType = binaryExpr.getOperator().getType();

        MultiKeyMap operationAndTypeMapForInstruction = new MultiKeyMap();
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.MULT, DataType.INT), MULI);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.MULT, DataType.FLOAT), MULF);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.DIV, DataType.INT), DIVI);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.DIV, DataType.FLOAT), DIVF);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.ADD, DataType.INT), ADDI);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.ADD, DataType.FLOAT), ADDF);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.SUB, DataType.INT), SUBI);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.SUB, DataType.FLOAT), SUBF);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.LESS, DataType.INT), LTI);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.LESS, DataType.FLOAT), LTF);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.LESS_EQUAL, DataType.INT), LEI);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.LESS_EQUAL, DataType.FLOAT), LEF);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.GREATER, DataType.INT), GTI);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.GREATER, DataType.FLOAT), GTF);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.GREATER_EQUAL, DataType.INT), GEI);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.GREATER_EQUAL, DataType.FLOAT), GEF);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.EQUAL_EQUAL, DataType.INT), EQI);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.EQUAL_EQUAL, DataType.BOOL), EQI); // TODO
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.EQUAL_EQUAL, DataType.FLOAT), EQF);
        operationAndTypeMapForInstruction.put(new MultiKey(OperationType.MOD, DataType.INT), MOD);

        InstructionType resolvedInstruction = (InstructionType) operationAndTypeMapForInstruction.get(new MultiKey(operationType, type));
        if(resolvedInstruction == null) {
            throw new UnsupportedOperationException(String.format("Operation '%s' not possible with type %s", operationType, type));
        }
        interRepresentation.write(resolvedInstruction);
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
        DataType type = literalExpr.getType();
        Object value = literalExpr.getValue();

        switch (type) {
            case INT:
                interRepresentation.write(PUSHI, (Integer) value);
                break;
            case CHAR:
                interRepresentation.write(PUSHC, (Character) value);
                break;
            case STRING:
                int key = interRepresentation.addString((String) value);
                interRepresentation.write(PUSHS, key);
                break;
            case BOOL:
                interRepresentation.write(PUSHB, ((boolean) value) ? 1 : 0);
                break;
            case FLOAT:
                long doubleToLong = Double.doubleToRawLongBits((Double) value);
                //interRepresentation.write(PUSHF, doubleToLong);
                break;
            case NULL:
                interRepresentation.write(PUSHB, 0); // TODO null should be revised when pointers are implemented
                break;
            case VOID:
                throw new UnsupportedOperationException("Void type literals should be impossible. Value = '" + literalExpr.getValue() + "'");
            default:
                throw new UnsupportedOperationException("Unsupported literal type " + type);
        }

        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary unaryExpr) {
        // TODO: optimisation, if unaryExpr member is literal, somehow replace it value-with-modification
        OperatorToken operator = unaryExpr.getOperator();
        switch (operator.getType()) {

            case ADD:
                break;
            case SUB:
                break;
            case NOT:
                break;
            case INC_PRE:
                break;
            case INC_POST:
                break;
            case DEC_PRE:
                break;
            case DEC_POST:
                break;
            default:
                throw new UnsupportedOperationException(String.format("Operation type '%s' is not supported in unary operations in line %d", operator.getType(), operator.getLine()));
        }
        // TODO
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable variableExpr) {
        Scope scope = variableExpr.getScope();
        Token variableName = variableExpr.getName();
        StackDeclaredNode resolvedVariable = (StackDeclaredNode) scope.resolve(variableName, ElementType.VARIABLE);
        int pointer = resolvedVariable.getStackSlot();

        interRepresentation.write(PEEK, pointer);
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
        interRepresentation.write(PUSHI, label);
        interRepresentation.write(PUSHI, 999);
        interRepresentation.write(PUSHI, 999);
        List<Expr> arguments = callExpr.getArguments();
        for (Expr argument : arguments) {
            argument.accept(this);
        }
        interRepresentation.write(CALL, arguments.size());
        return null;
    }

    @Override
    public Void visitArrayStmt(Stmt.Array arrayStmt) {
        // TODO
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
