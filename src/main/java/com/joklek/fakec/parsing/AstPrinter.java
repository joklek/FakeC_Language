package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.IExpr;
import com.joklek.fakec.parsing.ast.IStmt;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.types.data.DataType;
import com.joklek.fakec.tokens.Token;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    public String print(Expr expr) {
        return expr.accept(this);
    }

    public String print(Stmt.Program program) {
        return program.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary binaryExpr) {
        String left = buildBranch("Left: ", binaryExpr.getLeft());
        String right = buildBranch("Right: ", binaryExpr.getRight());
        return String.format("BinaryExpr(%s):%n" +
                               "%s" +
                               "%s", binaryExpr.getOperator().getType(), left, right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping groupingExpr) {
        return buildBranch("", groupingExpr.getExpression());
    }

    @Override
    public String visitLiteralExpr(Expr.Literal literalExpr) {
        if (literalExpr.getValue() == null) {
            return "null" + System.lineSeparator();
        }
        return literalExpr.getValue().toString() + System.lineSeparator();
    }

    @Override
    public String visitVariableExpr(Expr.Variable variableExpr) {
        return variableExpr.getName().getLexeme() + System.lineSeparator();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary unaryExpr) {
        String output = String.format("UnaryExpr(%s):%n" +
                                      "Right: ", unaryExpr.getOperator().getType());
        return buildBranch(output, unaryExpr.getRight());
    }

    @Override
    public String visitAssignExpr(Expr.Assign assignExpr) {
        return buildBranch(String.format("AssignExpr: %n" +
                                         "VAR: %s%n" +
                                          "Value: ", assignExpr.getName().getLexeme()), assignExpr.getValue());
    }

    @Override
    public String visitCallExpr(Expr.Call callExpr) {
        StringBuilder builder = new StringBuilder(String.format("CallExpr: %s%n", callExpr.getIdent().getLexeme()));
        List<Expr> arguments = callExpr.getArguments();

        for (int i = 0; i < arguments.size(); i++) {
            Expr expression = arguments.get(i);
            builder.append(indent(buildBranch(String.format("args[%d]: ", i), expression)));
        }
        return builder.toString();
    }

    @Override
    public String visitArrayAccessExpr(Expr.ArrayAccess arrayAccessExpr) {
        return buildBranch(String.format("ArrayAccess: %s%n" +
                             "Offset: ", arrayAccessExpr.getArray().getLexeme()), arrayAccessExpr.getOffset());
    }

    @Override
    public String visitArrayCreateExpr(Expr.ArrayCreate arrayCreateExpr) {
        return buildBranch(String.format("ArrayCreation: %s%n" +
                "Size: ", arrayCreateExpr.getArray().getLexeme()), arrayCreateExpr.getSize());
    }

    @Override
    public String visitProgramStmt(Stmt.Program program) {
        StringBuilder builder = new StringBuilder(String.format("PROGRAM: %n"));
        List<Stmt.Function> functions = program.getFunctions();

        for (int i = 0; i < functions.size(); i++) {
            Stmt.Function function = functions.get(i);
            builder.append(indent(buildBranch(String.format("FUNC[%d]: ", i), function)));
        }
        return builder.toString();
    }

    @Override
    public String visitFunctionStmt(Stmt.Function functionStmt) {
        List<Pair<Token, DataType>> params = functionStmt.getParams();
        int count = 0;
        StringBuilder paramsInfo = new StringBuilder();
        for(Pair<Token, DataType> param: params) {
            paramsInfo.append(String.format("PARAM[%d]%n", count));
            String paramInfo = String.format("Name: %s%n" +
                    "Type: %s%n", param.getKey().getLexeme(), param.getValue());
            paramsInfo.append(indent(paramInfo));
            count++;
        }
        String paramsTree = paramsInfo.toString();

        String blockTree = visitBlockStmt(functionStmt.getBody());
        return String.format("Name: %s%n" +
                                      "Type: %s%n" +
                                      "%s" +
                                      "%s", functionStmt.getName().getLexeme(), functionStmt.getType(), paramsTree, blockTree);
    }

    // TODO Fix megaindent
    @Override
    public String visitReturnStmt(Stmt.Return returnStmt) {
        return returnStmt.hasValue()
                ? buildBranch("RETURN: ", returnStmt.getValue())
                : "RETURN" + System.lineSeparator();
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression expressionStmt) {
        return expressionStmt.getExpression().accept(this);
    }

    @Override
    public String visitIfStmt(Stmt.If ifStmt) {
        List<Pair<IExpr, Stmt.Block>> branches = ifStmt.getBranches();

        StringBuilder builder = new StringBuilder(String.format("IF:%n"));
        int position = 0;
        for (Pair<IExpr, Stmt.Block> conditionAndBody : branches) {
            String condition = buildBranch("Condition: ", conditionAndBody.getKey());
            String body = conditionAndBody.getValue().getStatements().isEmpty() ? "Body:" + System.lineSeparator() : buildBranch("Body: ", conditionAndBody.getValue());
            String branchInfo = String.format("Branch[%d]: %n" +
                    "%s" +
                    "%s", position, indent(condition), indent(body));
            builder.append(branchInfo);
            position++;
        }

        if (ifStmt.getElseBranch() != null) {
            String elseBranch = ifStmt.getElseBranch().getStatements().isEmpty() ? "Else:" + System.lineSeparator() : buildBranch("Else: ", ifStmt.getElseBranch());
            builder.append(elseBranch);
        }

        return builder.toString();
    }

    @Override
    public String visitWhileStmt(Stmt.While whileStmt) {
        String condition = buildBranch("CONDITION: ", whileStmt.getCondition());
        String body = buildBranch("BODY: ", whileStmt.getBody());
        return String.format("WHILE: %n" +
                               "%s" +
                               "%s", condition, body);
    }

    @Override
    public String visitOutputStmt(Stmt.Output outputStmt) {
        StringBuilder builder = new StringBuilder(String.format("OUTPUT: %n"));
        List<IExpr> expressions = outputStmt.getExpressions();

        for (int i = 0; i < expressions.size(); i++) {
            IExpr expr = expressions.get(i);
            builder.append(indent(buildBranch(String.format("PrintedExpr[%d]: ", i), expr)));
        }
        return builder.toString();
    }

    @Override
    public String visitInputStmt(Stmt.Input inputStmt) {
        StringBuilder builder = new StringBuilder(String.format("INPUT: %n"));
        List<Token> variables = inputStmt.getVariables();

        for (int i = 0; i < variables.size(); i++) {
            Token variable = variables.get(i);
            builder.append(indent(String.format("InputVar[%d]: %s%n", i, variable.getLexeme())));
        }
        return builder.toString();
    }

    @Override
    public String visitVarStmt(Stmt.Var varStmt) {
        String outputPart = String.format("Type: %s%n" +
                                          "Name: %s%n" +
                                          "Initializer: ", varStmt.getType(), varStmt.getName().getLexeme());
        String output = outputPart;
        if(varStmt.getInitializer() != null) {
            output = buildBranch(outputPart, varStmt.getInitializer());
        }
        else {
            output = output + "null" + System.lineSeparator();
        }
        return String.format("VarStmt:%n" +
                               "%s", output);
    }

    @Override
    public String visitArrayStmt(Stmt.Array arrayStmt) {
        String outputPart = String.format("Type: %s[]%n" +
                "Name: %s%n" +
                "Initializer: ", arrayStmt.getType(), arrayStmt.getName().getLexeme());
        String output = outputPart;
        if(arrayStmt.getInitializer() != null) {
            output = buildBranch(outputPart, arrayStmt.getInitializer());
        }
        else {
            output = output + "null" + System.lineSeparator();
        }
        return String.format("VarStmt:%n" +
                "%s", output);
    }

    @Override
    public String visitBlockStmt(Stmt.Block blockStmt) {
        StringBuilder statementsInfo = new StringBuilder();
        List<IStmt> body = blockStmt.getStatements();
        for(int i = 0; i < body.size(); i++) {
            String stmtInfo = buildBranch(String.format("STMT[%d]: ", i), body.get(i));
            statementsInfo.append(stmtInfo);
        }
        return statementsInfo.toString();
    }

    @Override
    public String visitBreakStmt(Stmt.Break breakStmt) {
        return "BREAK" + System.lineSeparator();
    }

    @Override
    public String visitContinueStmt(Stmt.Continue continueStmt) {
        return "CONTINUE" + System.lineSeparator();
    }

    private String buildBranch(String name, IExpr... exprs) {
        StringBuilder builder = new StringBuilder(name);

        for (IExpr expr : exprs) {
            String part = expr != null ? expr.accept(this) : "null" + System.lineSeparator();
            builder.append(indentAllButFirst(part));
        }
        return builder.toString();
    }

    private String buildBranch(String name, IStmt... stmts) {
        StringBuilder builder = new StringBuilder(name);

        for (IStmt stmt : stmts) {
            String part = stmt.accept(this);
            builder.append(indentAllButFirst(part));
        }

        return builder.toString();
    }

    /**
     * Indents every line by two spaces
     * @param unindented not yet indented string
     * @return indented string
     */
    private String indent(String unindented) {
        return unindented.replaceAll("(?m)^", "  ");
    }

    private String indentAllButFirst(String unindented) {
        return indent(unindented).replaceFirst("\\s{2}", "");
    }
}
