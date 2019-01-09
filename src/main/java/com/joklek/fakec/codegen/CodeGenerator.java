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

import java.util.List;

import static com.joklek.fakec.codegen.InstructionType.*;

// 4.4
public class CodeGenerator implements Stmt.Visitor<Void>, Expr.Visitor<Void>  {

    private IntermediateRepresentation interRepresentation;
    private Label mainLabel;
    private final MultiKeyMap operationAndTypeMapForInstruction;

    public CodeGenerator() {
        this(new InstructionResolver());
    }

    public CodeGenerator(InstructionResolver resolver) {
        this.interRepresentation = new IntermediateRepresentation(resolver, new StringTable());
        this.operationAndTypeMapForInstruction = new MultiKeyMap();
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.MULT, DataType.INT), MULI);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.MULT, DataType.FLOAT), MULF);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.DIV, DataType.INT), DIVI);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.DIV, DataType.FLOAT), DIVF);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.ADD, DataType.INT), ADDI);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.ADD, DataType.FLOAT), ADDF);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.SUB, DataType.INT), SUBI);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.SUB, DataType.FLOAT), SUBF);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.LESS, DataType.INT), LTI);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.LESS, DataType.FLOAT), LTF);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.LESS_EQUAL, DataType.INT), LEI);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.LESS_EQUAL, DataType.FLOAT), LEF);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.GREATER, DataType.INT), GTI);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.GREATER, DataType.FLOAT), GTF);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.GREATER_EQUAL, DataType.INT), GEI);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.GREATER_EQUAL, DataType.FLOAT), GEF);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.EQUAL_EQUAL, DataType.INT), EQI);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.NOT_EQUAL, DataType.INT), NEI);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.NOT_EQUAL, DataType.FLOAT), NEF);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.EQUAL_EQUAL, DataType.BOOL), EQI); // TODO
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.EQUAL_EQUAL, DataType.FLOAT), EQF);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.MOD, DataType.INT), MOD);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.OR, DataType.BOOL), OR);
        this.operationAndTypeMapForInstruction.put(new MultiKey(OperationType.AND, DataType.BOOL), AND);
    }

    public IntermediateRepresentation generate(Stmt.Program program) {
        visitProgramStmt(program);
        return interRepresentation;
    }

    @Override
    public Void visitProgramStmt(Stmt.Program program) {
        mainLabel = interRepresentation.newLabel();
        // Were pushing these values to stack, because when call happens, they will be overwritten by register values
        // Better way for this would be pushing on the interpreter maybe?
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

        if(functionStmt.getType() == DataType.INT &&
                functionStmt.getName().getLexeme().equals("main") &&
                functionStmt.getParams().isEmpty()) {
            interRepresentation.placeLabel(mainLabel);
        }

        Label fnLabel = functionStmt.getLabel();
        interRepresentation.placeLabel(fnLabel);

        int innerVariableCount = functionStmt.getScope().getPointer().getCurrentStackSlot();
        if(innerVariableCount != 0) {
            interRepresentation.write(ALLOC, innerVariableCount);
        }

        /*for (Pair<Token, DataType> param : functionStmt.getParams()) {
            int pointer = ((StackDeclaredNode) functionStmt.getBody().getScope().resolve(param.getLeft(), ElementType.VARIABLE)).getStackSlot();
            interRepresentation.write(PEEK, pointer);
        }*/
        // TODO fix this

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
            case CHAR:
            case STRING:
            case BOOL:
                interRepresentation.write(POP);
                break;
            case FLOAT:
                interRepresentation.write(POPF);
                break;
            case NULL:
                interRepresentation.write(POP); // TODO null should be revised when pointers are implemented
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
        List<Pair<IExpr, Stmt.Block>> branches = ifStmt.getBranches();
        for (Pair<IExpr, Stmt.Block> branch : branches) {
            IExpr condition = branch.getLeft();
            Stmt.Block block = branch.getRight();

            Label label = interRepresentation.newLabel();
            condition.accept(this);

            interRepresentation.write(JMPZ, label);
            block.accept(this);

            // Optimisation to remove last jump from an if it it's pointing to the following instruction
            //if(branches.lastIndexOf(branch) != branches.size() - 1 || ifStmt.getElseBranch() != null) {
                interRepresentation.write(JMP, endLabel);
                interRepresentation.placeLabel(label);
            //}
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
            interRepresentation.write(STDIN);
            interRepresentation.write(POKE, variableNode.getStackSlot());
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var varStmt) {

        if (varStmt.getInitializer() != null) {
            varStmt.getInitializer().accept(this);
            interRepresentation.write(POKE, varStmt.getStackSlot());
            interRepresentation.write(POP);
        }
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break breakStmt) {
        Label label = breakStmt.getTarget().getEndLabel();
        interRepresentation.write(JMP, label);
        return null;
    }

    // TODO Does not work with for loops, fFFF
    @Override
    public Void visitContinueStmt(Stmt.Continue continueStmt) {
        Label label = continueStmt.getTarget().getStartLabel();
        interRepresentation.write(JMP, label);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary binaryExpr) {

        binaryExpr.getLeft().accept(this);
        binaryExpr.getRight().accept(this);
        DataType type = binaryExpr.getLeft().getType();
        OperationType operationType = binaryExpr.getOperator().getType();

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
        DataType type = literalExpr.getType();
        Object value = literalExpr.getValue();

        switch (type) {
            case INT:
                interRepresentation.write(PUSHI, (Integer) value);
                break;
            case CHAR:
                interRepresentation.write(PUSHI, (Character) value);
                break;
            case STRING:
                int key = interRepresentation.addString((String) value);
                interRepresentation.write(PUSHI, key);
                break;
            case BOOL:
                interRepresentation.write(PUSHI, ((boolean) value) ? 1 : 0);
                break;
            case FLOAT:
                int floatToInt = Float.floatToIntBits((float) value);
                interRepresentation.write(PUSHF, floatToInt);
                break;
            case NULL:
                interRepresentation.write(PUSHI, 0); // TODO null should be revised when pointers are implemented
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

        if(!(unaryExpr.getRight() instanceof Expr.Variable)) {
            return null;
        }
        Expr.Variable var = (Expr.Variable) unaryExpr.getRight();

        OperationType operationType = operator.getType();


        int pointer = ((StackDeclaredNode)var.getScope().resolve(var.getName(), ElementType.VARIABLE)).getStackSlot();
        unaryExpr.getRight().accept(this);

        // TODO Decide how this should work
        if(operationType == OperationType.INC_PRE || operationType == OperationType.DEC_PRE) {
            InstructionType unaryOp = operationType == OperationType.INC_PRE ? ADDI : SUBI;
            interRepresentation.write(PUSHI, 1);
            interRepresentation.write(PEEK, pointer);
            interRepresentation.write(unaryOp);
            interRepresentation.write(POKE, pointer);
            return null;
        }
        else if(operationType == OperationType.INC_POST || operationType == OperationType.DEC_POST) {
            InstructionType unaryOp = operationType == OperationType.INC_POST ? ADDI : SUBI;
            interRepresentation.write(PUSHI, 1);
            interRepresentation.write(PEEK, pointer);
            interRepresentation.write(unaryOp);
            interRepresentation.write(POKE, pointer);
            return null;
        }
        // Add is ignored as it is useless
        else if(operationType == OperationType.ADD) {
            return null;
        }
        else if(operationType == OperationType.SUB) {
            InstructionType pushType;
            InstructionType multType;
            switch (var.getType()) {
                case INT:
                    pushType = PUSHI;
                    multType =  MULI;
                    break;
                case FLOAT:
                    pushType = PUSHF;
                    multType =  MULF;
                    break;
                default:
                    throw new UnsupportedOperationException(String.format("Operation type '%s' is not supported in unary operations in line %d", operationType, operator.getLine()));
            }
            interRepresentation.write(pushType, -1);
            interRepresentation.write(PEEK, pointer);
            interRepresentation.write(multType);
            interRepresentation.write(POKE, pointer);
            return null;
        }
        else if(operationType == OperationType.NOT) {
            interRepresentation.write(PEEK, pointer);
            interRepresentation.write(NOT);
            interRepresentation.write(POKE, pointer);
            return null;
        }
        throw new UnsupportedOperationException(String.format("Operation type '%s' is not supported in unary operations in line %d", operationType, operator.getLine()));
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

    @Override
    public Void visitRandom(Expr.Random random) {
        random.getMaxInclusive().accept(this);
        random.getMinInclusive().accept(this);
        interRepresentation.write(RND);
        return null;
    }
}
