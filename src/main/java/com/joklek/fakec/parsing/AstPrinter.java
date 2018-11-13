package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;

import java.util.List;
import java.util.Map;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    public String print(Expr expr) {
        return expr.accept(this);
    }

    public String print(Stmt.Program program) {
        return program.accept(this);
    }

    public String print(Stmt stmt) {
        return stmt.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        String left = buildBranch("Left: ", expr.getLeft());
        String right = buildBranch("Right: ", expr.getRight());
        return String.format("BinaryExpr(%s):%n" +
                               "%s%n" +
                               "%s%n", expr.getOperator().getType(), left, right);
    }

    // TODO
    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return buildBranch("group", expr.getExpression());
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.getValue() == null) {
            return "null";
        }
        return expr.getValue().toString();
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.getName().getLexeme();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        String left = buildBranch("Left: ", expr.getLeft());
        String right = buildBranch("Right: ", expr.getRight());
        return String.format("LogicalExpr(%s):%n" +
                "%s%n" +
                "%s", expr.getOperator().getType(), left, right);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        String output = String.format("UnaryExpr(%s):%n" +
                                      "Right: ", expr.getOperator().getType());
        return buildBranch(output, expr.getRight());
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return buildBranch(String.format("AssignExpr: %n" +
                                         "VAR: %s%n" +
                                          "Value: ", expr.getName().getLexeme()), expr.getValue());
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        StringBuilder builder = new StringBuilder(String.format("CallExpr: %n"));
        List<Expr> arguments = expr.getArguments();

        for (int i = 0; i < arguments.size(); i++) {
            Expr expression = arguments.get(i);
            builder.append(indent(buildBranch(String.format("args[%d]: ", i), expression)));
        }
        return builder.toString();
    }

    @Override
    public String visitProgramStmt(Stmt.Program stmt) {
        StringBuilder builder = new StringBuilder(String.format("PROGRAM: %n"));
        List<Stmt.Function> functions = stmt.getFunctions();

        for (int i = 0; i < functions.size(); i++) {
            Stmt.Function function = functions.get(i);
            builder.append(indent(buildBranch(String.format("FUNC[%d]: ", i), function)));
        }
        return builder.toString();
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        Map<Token, TokenType> params = stmt.getParams();
        int count = 0;
        StringBuilder paramsInfo = new StringBuilder();
        for(Map.Entry<Token, TokenType> param: params.entrySet()) {
            paramsInfo.append(String.format("PARAM[%d]%n", count));
            String paramInfo = String.format("Name: %s%n" +
                    "Type: %s%n", param.getKey().getLexeme(), param.getValue());
            paramsInfo.append(indent(paramInfo));
            count++;
        }
        String paramsTree = paramsInfo.toString();

        String blockTree = visitBlockStmt(stmt.getBody());
        return String.format("Name: %s%n" +
                                      "Type: %s%n" +
                                      "%s" +
                                      "%s", stmt.getName().getLexeme(), stmt.getType(), paramsTree, blockTree);
    }


    // TODO Fix megaindent
    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        return stmt.getValue() == null
                ? "RETURN"
                : buildBranch("RETURN: ", stmt.getValue());
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return stmt.getExpression().accept(this);
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        stmt.getCondition();
        stmt.getElseBranch();
        stmt.getThenBranch();
        return null;
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        String condition = buildBranch("CONTIDITION: ", stmt.getCondition());
        String body = buildBranch("BODY: ", stmt.getBody());
        return String.format("WHILE: %n" +
                               "%s" +
                               "%s", condition, body);
    }

    @Override
    public String visitOutputStmt(Stmt.Output stmt) {
        StringBuilder builder = new StringBuilder(String.format("OUTPUT: %n"));
        List<Expr> expressions = stmt.getExpressions();

        for (int i = 0; i < expressions.size(); i++) {
            Expr expr = expressions.get(i);
            builder.append(indent(buildBranch(String.format("PrintedExpr[%d]: ", i), expr)));
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }

    @Override
    public String visitInputStmt(Stmt.Input stmt) {
        StringBuilder builder = new StringBuilder(String.format("INPUT: %n"));
        List<Token> variables = stmt.getVariables();

        for (int i = 0; i < variables.size(); i++) {
            Token variable = variables.get(i);
            builder.append(indent(String.format("InputVar[%d]: %s%n", i, variable.getLexeme())));
        }
        return builder.toString();
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        String outputPart = String.format("Type: %s%n" +
                                          "Name: %s%n" +
                                          "Initializer: ", stmt.getType(), stmt.getName().getLexeme());
        String output = buildBranch(outputPart, stmt.getInitializer());
        return String.format("VarStmt:%n" +
                               "%s", output);
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder statementsInfo = new StringBuilder();
        List<Stmt> body = stmt.getStatements();
        for(int i = 0; i < body.size(); i++) {
            String stmtInfo = buildBranch(String.format("STMT[%d]: ", i), body.get(i));
            statementsInfo.append(stmtInfo);
            //statementsInfo.append(System.lineSeparator());
        }
        return statementsInfo.toString();
    }

    @Override
    public String visitBreakStmt(Stmt.Break stmt) {
        return "BREAK";
    }

    @Override
    public String visitContinueStmt(Stmt.Continue stmt) {
        return "CONTINUE";
    }

    private String buildBranch(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder(name);

        for (Expr expr : exprs) {
            String part = expr.accept(this);
            builder.append(indentAllButFirst(part));
        }
        return builder.toString();
    }

    private String buildBranch(String name, Stmt... stmts) {
        StringBuilder builder = new StringBuilder(name);

        for (Stmt stmt : stmts) {
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
