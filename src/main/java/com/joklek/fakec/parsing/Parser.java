package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.error.ParserError;
import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;

import java.util.*;

import static com.joklek.fakec.tokens.TokenType.*;

@SuppressWarnings("WeakerAccess")
public class Parser {

    private List<Token> tokens;
    private List<ParserError> errors;
    private int offset;

    private static final TokenType[] FUNCTION_TYPES = {STRING_TYPE, FLOAT_TYPE, CHAR_TYPE, INT_TYPE, BOOL_TYPE, VOID_TYPE};
    private static final TokenType[] VARIABLE_TYPES = {STRING_TYPE, FLOAT_TYPE, CHAR_TYPE, INT_TYPE, BOOL_TYPE};

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.errors = new ArrayList<>();
        this.offset = 0;
    }

    // <program> ::= {<function>}<EOF>
    public Stmt.Program parseProgram() {
        List<Stmt.Function> functions = new ArrayList<>();
        while(current().getType() != EOF) {
            functions.add(parseFunction());
        }
        return new Stmt.Program(functions);
    }

    // <fn_type_specifier> <identifier> <fn_params> <block>
    protected Stmt.Function parseFunction() {
        TokenType type = consume(FUNCTION_TYPES, "Functions should start with type").getType();
        Token name = consume(IDENTIFIER, "Expect function name.");

        Map<Token, TokenType> parameters = parseParams();

        if(!check(CURLY_LEFT) ){
            throw error(current(), "Expect '{' before function body.");
        }

        List<Stmt> body = parseBlock();
        return new Stmt.Function(type, name, parameters, body);
    }

    // <fn_params> ::= "(" [<parameter> {"," <parameter>}] ")"
    //   <parameter> ::= <variable_type_specifier> <identifier>
    private Map<Token, TokenType> parseParams() {
        consume(LEFT_PAREN, "Expect '(' after function name.");
        Map<Token, TokenType> parameters = new HashMap<>();
        if (!check(RIGHT_PAREN)) {
            do {
                TokenType parameterType = consume(VARIABLE_TYPES, "Parameter should start with type").getType();
                Token parameterName = consume(IDENTIFIER, "Expect parameter name.");
                if(parameters.containsKey(parameterName)) {
                    throw error(parameterName, String.format("Function parameter names should be unique, but '%s' is repeated", parameterName.getLexeme()));
                }
                parameters.put(parameterName, parameterType);
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after function parameters.");
        return parameters;
    }

    // TODO can handle block nesting?
    // <block> ::= "{" {statement} "}"
    protected List<Stmt> parseBlock() {
        consume(CURLY_LEFT, "Expect '{' for block start.");
        List<Stmt> statements = new ArrayList<>();

        while (!check(CURLY_RIGHT) && !isAtEnd()) {
            statements.add(parseStatement());
        }

        consume(CURLY_RIGHT, "Expect '}' after block.");
        return statements;
    }

    // <statement>  ::= <expression> ";" | <exit_keyword> ";"| "return" <expression> ";"| <assigment_statement> ";"| <var_declaration> ";"| <io_statement> ";" | <while_statement> | <for_statement> | <if_statements>
    protected Stmt parseStatement() {
        TokenType type = current().getType();

        switch (type) {
            case RETURN:
                return parseReturntStmt();
            case BREAK:
                advance();
                consume(SEMICOLON, "Unclosed continue statement, semicolon is missing");
                return new Stmt.Break(previous());
            case CONTINUE:
                advance();
                consume(SEMICOLON, "Unclosed continue statement, semicolon is missing");
                return new Stmt.Continue(previous());
            case WHILE:
                return whileStatement();
            case FOR:
                return forStatement();
            case IF:
                return ifStatement();
            case INPUT:
                return parseInput();
            case OUTPUT:
                return printStatement();
            default:
                if(Arrays.asList(VARIABLE_TYPES).contains(type)) {
                    return parseVarDecStmt();
                }
                return expressionStatement();
        }
    }

    protected Stmt parseVarDecStmt() {
        TokenType type = consume(VARIABLE_TYPES, "Expect variable type.").getType();
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(type, name, initializer);
    }

    protected Stmt parseReturntStmt() {
        Token keyword = consume(RETURN);

        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    protected Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(INT_TYPE)) {
            initializer = parseVarDecStmt();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        Stmt body = parseStatement();

        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.Expression(increment)));
        }

        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    protected Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = parseStatement();

        return new Stmt.While(condition, body);
    }

    protected Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = parseStatement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = parseStatement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    // <output_statement> ::= "output" "<<" <argument_list>
    protected Stmt printStatement() {
        consume(OUTPUT_SIGN, "Output sign << should follow output keyword");
        List<Expr> printExpressions = new ArrayList<>();
        printExpressions.add(expression());
        while (current().getType() == COMMA) {
            printExpressions.add(expression());
        }
        consume(SEMICOLON, "Expect ';' after values.");
        return new Stmt.Print(printExpressions);
    }

    // <input_statement> ::= "input" ">>" <identifier> {"," <identifier>}
    private Stmt parseInput() {
        consume(INPUT_SIGN, "Input sign >> should follow input keyword");
        List<Token> inputTokens = new ArrayList<>();
        inputTokens.add(consume(IDENTIFIER, "Input and only accept variables"));
        while (current().getType() == COMMA) {
            inputTokens.add(consume(IDENTIFIER, "Input and only accept variables"));
        }
        consume(SEMICOLON, "Expect ';' after variables.");
        return new Stmt.Output(inputTokens);
    }

    protected Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    protected Expr expression() {
        return assignment();
    }

    protected Expr assignment() {
        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).getName();
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    protected Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    protected Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    protected Expr equality() {
        Expr expr = comparison();

        while (match(NOT_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    protected Expr comparison() {
        Expr expr = addition();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    protected Expr addition() {
        Expr expr = multiplication();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    protected Expr multiplication() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    protected Expr unary() {
        if (match(NOT, MINUS, PLUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    protected Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else {
                break;
            }
        }
        return expr;
    }

    protected Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    protected Expr primary() {
        if (match(FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(NULL)) {
            return new Expr.Literal(null);
        }
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(INTEGER, FLOAT, STRING)) {
            return new Expr.Literal(previous().getLiteral());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(current(), "Expect expression.");
    }


    private Token consume(TokenType type) {
        if (check(type)) {
            return advance();
        }

        throw error(current(), "Expected: " + type + "was: " + current().getType());
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }

        throw error(current(), message);
    }

    private Token consume(TokenType[] types, String message) {
        for(TokenType type: types) {
            if (check(type)) {
                return advance();
            }
        }
        throw error(current(), message);
    }

    private ParserError error(Token token, String message) {
        ParserError error = new ParserError(message, token);
        errors.add(error);
        com.joklek.fakec.Compiler.error(token, message);
        return error;
    }

    /**
     * Discard tokens until possible statement boundary
     */
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().getType() == SEMICOLON) {
                return;
            }

            switch (current().getType()) {
                case INT_TYPE:
                case FLOAT_TYPE:
                case CHAR_TYPE:
                case BOOL_TYPE:
                case VOID_TYPE:
                case STRING_TYPE:
                case FOR:
                case IF:
                case WHILE:
                case INPUT:
                case OUTPUT:
                case RETURN:
                    return;
            }
            advance();
        }
    }

    /**
     * Advances if the current token is of any given type
     * @param types token types, which are expected to be as the current token type
     * @return true if step occurred
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if current token is of given type
     * @param type type to check current token
     * @return true if current token is of given type
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return current().getType() == type;
    }

    /**
     * Consumes and returns current token, then advances
     * @return current token
     */
    private Token advance() {
        if (!isAtEnd()) {
            offset++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return current().getType() == EOF;
    }

    private Token current() {
        return tokens.get(offset);
    }

    private Token previous() {
        return tokens.get(offset - 1);
    }
}
