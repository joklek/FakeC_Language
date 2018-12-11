package com.joklek.fakec.parsing.ast;

import com.joklek.fakec.codegen.Label;
import com.joklek.fakec.error.Error;
import com.joklek.fakec.parsing.types.data.DataType;
import com.joklek.fakec.scope.Scope;
import com.joklek.fakec.tokens.Token;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class Stmt implements IStmt {

    private Scope scope = null;
    public Scope getScope() {
        return scope;
    }
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    private IStmt parent = null;
    public IStmt getParent() {
        return parent;
    }
    public void setParent(IStmt parent) {
        this.parent = parent;
    }


    public static class Program extends Stmt {

        private final List<Function> functions;

        public Program(List<Function> functions) {
            this.functions = functions;
        }

        public List<Function> getFunctions() {
            return functions;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitProgramStmt(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitProgramStmt(this, errors);
        }
    }

    public static class Function implements IStmt, StackDeclaredNode, NodeWithLabel {

        private final DataType type;
        private final Token name;
        private final List<Pair<Token, DataType>> params;
        private final Block body;
        private final List<Return> returnStmts;
        private final Label label;
        private int stackSlot;
        private Scope scope = null;
        private IStmt parent = null;

        public Function(DataType type, Token name, List<Pair<Token, DataType>> params, Block body) {
            this.type = type;
            this.name = name;
            this.params = params;
            this.body = body;
            this.returnStmts = new ArrayList<>();
            this.label = new Label(); // TODO figure out if this is correct
        }

        public DataType getType() {
            return type;
        }

        public Token getName() {
            return name;
        }

        public List<Pair<Token, DataType>> getParams() {
            return params;
        }

        public Block getBody() {
            return body;
        }

        public List<Return> getReturnStmts() {
            return returnStmts;
        }

        public Label getLabel() {
            return label;
        }

        public Scope getScope() {
            return scope;
        }
        public void setScope(Scope scope) {
            this.scope = scope;
        }

        public IStmt getParent() {
            return parent;
        }
        public void setParent(IStmt parent) {
            this.parent = parent;
        }

        @Override
        public int getStackSlot() {
            return stackSlot;
        }

        @Override
        public void setStackSlot(int stackSlot) {
            this.stackSlot = stackSlot;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitFunctionStmt(this, errors);
        }
    }

    public static class Block extends Stmt {

        private final List<IStmt> statements;

        public Block(List<IStmt> statements) {
            this.statements = statements;
        }

        public List<IStmt> getStatements() {
            return statements;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitBlockStmt(this, errors);
        }
    }

    public static class Return extends Stmt {

        private final Token keyword;
        private final IExpr value;
        private final boolean hasValue;
        private Function target;

        public Return(Token keyword, IExpr value, boolean hasValue) {
            this.keyword = keyword;
            this.value = value;
            this.hasValue = hasValue;
        }

        public Token getKeyword() {
            return keyword;
        }

        @Nullable
        public IExpr getValue() {
            return value;
        }

        public boolean hasValue() {
            return hasValue;
        }

        public Function getTarget() {
            return target;
        }

        public void setTarget(Function target) {
            this.target = target;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitReturnStmt(this, errors);
        }
    }

    public static class Expression extends Stmt {

        private final IExpr expression;

        public Expression(IExpr expression) {
            this.expression = expression;
        }

        public IExpr getExpression() {
            return expression;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitExpressionStmt(this, errors);
        }
    }

    public static class If extends Stmt {

        private final List<Pair<IExpr, Block>> branches;
        private final Block elseBranch;

        public If(List<Pair<IExpr, Block>> branches, Block elseBranch) {
            this.branches = branches;
            this.elseBranch = elseBranch;
        }

        public List<Pair<IExpr, Block>> getBranches() {
            return branches;
        }

        @Nullable
        public Block getElseBranch() {
            return elseBranch;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitIfStmt(this, errors);
        }
    }

    public static class While extends Stmt {

        private final IExpr condition;
        private final Block body;
        private Label startLabel;
        private Label endLabel;

        public While(IExpr condition, Block body) {
            this.condition = condition;
            this.body = body;
            this.startLabel = new Label(); // TODO figure this out
            this.endLabel = new Label();     // TODO figure this out
        }

        public IExpr getCondition() {
            return condition;
        }

        public Block getBody() {
            return body;
        }

        public Label getStartLabel() {
            return startLabel;
        }

        public void setStartLabel(Label label) {
            this.startLabel = label;
        }

        public Label getEndLabel() {
            return endLabel;
        }

        public void setEndLabel(Label label) {
            this.endLabel = label;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitWhileStmt(this, errors);
        }
    }

    public static class Output extends Stmt {

        private final List<IExpr> expressions;

        public Output(List<IExpr> expressions) {
            this.expressions = expressions;
        }

        public List<IExpr> getExpressions() {
            return expressions;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitOutputStmt(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitOutputStmt(this, errors);
        }
    }

    public static class Input extends Stmt {

        private final List<Token> variables;

        public Input(List<Token> variables) {
            this.variables = variables;
        }

        public List<Token> getVariables() {
            return variables;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitInputStmt(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitInputStmt(this, errors);
        }
    }

    public static class Var implements IStmt, StackDeclaredNode, NodeWithLabel {

        private final DataType type;
        private final Token name;
        private final IExpr initializer;
        private Scope scope = null;
        private IStmt parent = null;
        private Label label;
        private int stackSlot;

        public Var(DataType type, Token name, IExpr initializer) {
            this.type = type;
            this.name = name;
            this.initializer = initializer;
            this.label = new Label();
        }

        public DataType getType() {
            return type;
        }

        public Token getName() {
            return name;
        }

        @Nullable
        public IExpr getInitializer() {
            return initializer;
        }

        public Scope getScope() {
            return scope;
        }
        public void setScope(Scope scope) {
            this.scope = scope;
        }

        public IStmt getParent() {
            return parent;
        }
        public void setParent(IStmt parent) {
            this.parent = parent;
        }

        @Override
        public Label getLabel() {
            return label;
        }

        @Override
        public int getStackSlot() {
            return stackSlot;
        }

        @Override
        public void setStackSlot(int stackSlot) {
            this.stackSlot = stackSlot;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitVarStmt(this, errors);
        }
    }

    public static class Array implements IStmt, StackDeclaredNode {

        private final DataType type;
        private final Token name;
        private final IExpr initializer;
        private Scope scope = null;
        private IStmt parent = null;
        private int stackSlot;

        public Array(DataType type, Token name, IExpr initializer) {
            this.type = type;
            this.name = name;
            this.initializer = initializer;
        }

        public DataType getType() {
            return type;
        }

        public Token getName() {
            return name;
        }

        @Nullable
        public IExpr getInitializer() {
            return initializer;
        }

        public Scope getScope() {
            return scope;
        }
        public void setScope(Scope scope) {
            this.scope = scope;
        }

        public IStmt getParent() {
            return parent;
        }
        public void setParent(IStmt parent) {
            this.parent = parent;
        }

        @Override
        public int getStackSlot() {
            return stackSlot;
        }

        @Override
        public void setStackSlot(int stackSlot) {
            this.stackSlot = stackSlot;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayStmt(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitArrayStmt(this, errors);
        }
    }

    public static class Break extends Stmt {

        private final Token token;
        private While target;

        public Break(Token token) {
            this.token = token;
            this.target = null;
        }

        public Token getToken() {
            return token;
        }

        public While getTarget() {
            return target;
        }

        public void setTarget(While target) {
            this.target = target;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBreakStmt(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitBreakStmt(this, errors);
        }
    }

    public static class Continue extends Stmt {

        private final Token token;
        private While target;

        public Continue(Token token) {
            this.token = token;
            this.target = null;
        }

        public Token getToken() {
            return token;
        }

        public While getTarget() {
            return target;
        }

        public void setTarget(While target) {
            this.target = target;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitContinueStmt(this);
        }

        public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {
            return visitor.visitContinueStmt(this, errors);
        }
    }
}
