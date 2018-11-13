package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.error.ParserError;
import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;

import java.util.*;

import static com.joklek.fakec.tokens.TokenType.*;

@SuppressWarnings({"WeakerAccess", "squid:CommentedOutCodeLine"})
public class Parser {

    private List<Token> tokens;
    private List<ParserError> errors;
    private int offset;

    private static final TokenType[] FUNCTION_TYPES = {STRING_TYPE, FLOAT_TYPE, CHAR_TYPE, INT_TYPE, BOOL_TYPE, VOID_TYPE};
    private static final TokenType[] VARIABLE_TYPES = {STRING_TYPE, FLOAT_TYPE, CHAR_TYPE, INT_TYPE, BOOL_TYPE};
    private static final TokenType[] VARIABLES_OF_TYPE = {STRING, FLOAT, CHAR, INTEGER};

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

        Stmt.Block body = parseBlock();
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
    protected Stmt.Block parseBlock() {
        consume(CURLY_LEFT, "Expect '{' for block start.");
        List<Stmt> statements = new ArrayList<>();

        while (!check(CURLY_RIGHT) && !isAtEnd()) {
            statements.add(parseStatement());
        }

        consume(CURLY_RIGHT, "Expect '}' after block.");
        return new Stmt.Block(statements);
    }

    // <statement>  ::= <expression> ";" | <exit_keyword> ";"| "return" <expression> ";"| <assigment_statement> ";"| <var_declaration> ";"| <io_statement> ";" | <while_statement> | <for_statement> | <if_statements>
    protected Stmt parseStatement() {
        TokenType type = current().getType();

        switch (type) {
            case RETURN:
                return parseReturnStmt();
            case BREAK:
                advance();
                consume(SEMICOLON, "Unclosed continue statement, semicolon is missing");
                return new Stmt.Break(previous());
            case CONTINUE:
                advance();
                consume(SEMICOLON, "Unclosed continue statement, semicolon is missing");
                return new Stmt.Continue(previous());
            case WHILE:
                return parseWhile();
            case FOR:
                return forStatement();
            case IF:
                return ifStatement();
            case INPUT:
                return parseInput();
            case OUTPUT:
                return parseOutput();
            default:
                if(Arrays.asList(VARIABLE_TYPES).contains(type)) {
                    return parseVarDecStmt();
                }
                return parseExprStatement();
        }
    }

    // <atomic_declaration> ::= <type_atomic_specifier> <identifier> ["=" <expression>] {, <identifier> ["=" <expression>]}
    protected Stmt parseVarDecStmt() {
        TokenType type = consume(VARIABLE_TYPES, "Expect variable type.").getType();
        if(check(LEFT_BRACE)) {
            return parseArrayDecStmt(type);
        }

        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = parseExpression();
        }
        Stmt declaration = new Stmt.Var(type, name, initializer);

        if(check(COMMA)) {
            List<Stmt> declarations = new ArrayList<>();
            declarations.add(declaration);
            while(match(COMMA)) {
                name = consume(IDENTIFIER, "Expect variable name.");
                initializer = null;
                if (match(EQUAL)) {
                    initializer = parseExpression();
                }
                declarations.add(new Stmt.Var(type, name, initializer));
            }
            declaration = new Stmt.Block(declarations);
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return declaration;
    }

    // <array_declaration>  ::= <type_atomic_specifier> "[" "]" <identifier> ("["<expression>"]" | "=" <expression>)
    private Stmt parseArrayDecStmt(TokenType type) {
        consume(LEFT_BRACE);
        consume(RIGHT_BRACE, "Right brace should follow left brace in array declaration.");
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = parseArrayDeclarator(name);
        Stmt declaration = new Stmt.Array(type, name, initializer);

        if(check(COMMA)) {
            List<Stmt> declarations = new ArrayList<>();
            declarations.add(declaration);
            while(match(COMMA)) {
                name = consume(IDENTIFIER, "Expect variable name.");
                initializer =  parseArrayDeclarator(name);
                declarations.add(new Stmt.Array(type, name, initializer));
            }
            declaration = new Stmt.Block(declarations);
        }

        consume(SEMICOLON, "Expect ';' after array declaration.");
        return declaration;
    }

    private Expr parseArrayDeclarator(Token name) {
        Expr initializer = null;
        if (match(LEFT_BRACE)) {
            Expr size = parseExpression();
            consume(RIGHT_BRACE, "Right brace should follow array size declaration");
            initializer = new Expr.ArrayCreate(name, size);
        } else if (match(EQUAL)) {
            initializer = parseExpression();
        }
        return initializer;
    }

    protected Stmt parseReturnStmt() {
        Token keyword = consume(RETURN);

        Expr value = null;
        if (!check(SEMICOLON)) {
            value = parseExpression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    protected Stmt forStatement() {
        consume(FOR);
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (Arrays.asList(VARIABLE_TYPES).contains(current().getType())) {
            initializer = parseVarDecStmt();
        } else {
            initializer = parseExprStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = parseExpression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = parseExpression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        Stmt.Block body = parseBlock();
        // Adds increment part to statements
        if (increment != null) {
            List<Stmt> stmtList = new ArrayList<>(body.getStatements());
            stmtList.add(new Stmt.Expression(increment));
            body = new Stmt.Block(stmtList);
        }

        // if condition
        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        Stmt.While whileLoop = new Stmt.While(condition, body);

        // Adds initializer before while loop
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, whileLoop));
        }

        return body;
    }

    protected Stmt parseWhile() {
        consume(WHILE);
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = parseExpression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt.Block body = parseBlock();

        return new Stmt.While(condition, body);
    }

    protected Stmt ifStatement() {
        consume(IF);
        Map<Expr, Stmt.Block> branches = new HashMap<>();

        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = parseExpression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt.Block thenBranch = parseBlock();
        branches.put(condition, thenBranch);

        Stmt.Block elseBranch = null;

        while (check(ELSE) && peekType() == IF) {
            consume(ELSE);
            consume(IF);
            consume(LEFT_PAREN, "Expect '(' after 'if'.");
            condition = parseExpression();
            consume(RIGHT_PAREN, "Expect ')' after if condition.");
            thenBranch = parseBlock();
            branches.put(condition, thenBranch);
        }
        if (match(ELSE)) {
            elseBranch = parseBlock();
        }

        //return new Stmt.If(condition, thenBranch, elseBranch);

        return new Stmt.If(branches, elseBranch);
    }

    // <output_statement> ::= "output" "<<" <argument_list>
    protected Stmt parseOutput() {
        consume(OUTPUT);
        consume(OUTPUT_SIGN, "Output sign << should follow output keyword");
        List<Expr> printExpressions = new ArrayList<>();
        printExpressions.add(parseExpression());
        while (match(COMMA)) {
            printExpressions.add(parseExpression());
        }
        consume(SEMICOLON, "Expect ';' after values.");
        return new Stmt.Output(printExpressions);
    }

    // <input_statement> ::= "input" ">>" <identifier> {"," <identifier>}
    private Stmt parseInput() {
        consume(INPUT);
        consume(INPUT_SIGN, "Input sign >> should follow input keyword");
        List<Token> inputTokens = new ArrayList<>();
        inputTokens.add(consume(IDENTIFIER, "Input and only accept variables"));
        while (match(COMMA)) {
            inputTokens.add(consume(IDENTIFIER, "Input and only accept variables"));
        }
        consume(SEMICOLON, "Expect ';' after variables.");
        return new Stmt.Input(inputTokens);
    }

    protected Stmt parseExprStatement() {
        Expr expr = parseExpression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    // <expression> ::= <term4> {<or_op> <term4>}
    protected Expr parseExpression() {
        return assignment();
    }


    // TOOD FIX BNF
    protected Expr assignment() {
        Expr expr = parseOr();

        if (match(EQUAL, PLUS_EQUAL, MINUS_EQUAL, MUL_EQUAL, DIV_EQUAL, MOD_EQUAL)) {
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

    // <expression> ::= <term4> {<or_op> <term4>}
    protected Expr parseOr() {
        Expr expr = parseAnd();

        while (match(OR)) {
            Token operator = previous();
            Expr right = parseAnd();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // <term4> ::= <term3> {<and_op> <term3>}
    protected Expr parseAnd() {
        Expr expr = parseEquality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = parseEquality();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // <term3> ::= <term2> { <equality_op> <term2> }
    protected Expr parseEquality() {
        Expr expr = parseComparison();

        while (match(NOT_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = parseComparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // <term2> ::= <term1> { <comparison_op> <term1> }
    protected Expr parseComparison() {
        Expr expr = parseAddition();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = parseAddition();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // <term1> ::= <term0> {<sign_op> <term0>}
    protected Expr parseAddition() {
        Expr expr = parseMultiplication();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = parseMultiplication();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // <term0> ::= <term_postfix> {<mul_div_op> <term_postfix>}
    protected Expr parseMultiplication() {
        //Expr expr = parsePostfixExpr();
        Expr expr = parsePrefixExpr();

        while (match(SLASH, STAR, MOD)) {
            Token operator = previous();
            //Expr right = parsePostfixExpr();
            Expr right = parsePrefixExpr();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // TODO
    /*//<term_postfix> ::= <expression> <inc_dec_op> | <prefix_term>
    protected Expr parsePostfixExpr() {
        if(peekType() == INC || peekType() == DEC) {
            return new Expr.Assign(current(), new Expr.Binary(new Expr.Variable(current()), new Token(PLUS, current().getLine()), new Expr.Literal(1)));
        }
        else if(peekType() == DEC) {
            return new Expr.Assign(current(), new Expr.Binary(new Expr.Variable(current()), new Token(MINUS, current().getLine()), new Expr.Literal(1)));
        }
        return parsePrefixExpr();
    }*/

    // ++i ::=
    private Expr parsePrefixExpr() {
        if(match(INC, DEC) && check(IDENTIFIER)) {
            TokenType type = previous().getType() == INC ? PLUS : MINUS;
            Token identifier = consume(IDENTIFIER, "An identifier should follow a prefix operator");
            return new Expr.Assign(identifier, new Expr.Binary(new Expr.Variable(identifier), new Token(type, identifier.getLine()), new Expr.Literal(1)));
        }
        return parseNotExpr();
    }

    private Expr parseNotExpr() {
        Expr expr = null;
        while(match(NOT)) {
            expr = new Expr.Unary(previous(), parseNotExpr());
        }
        if (expr != null) {
            return expr;
        }
        return unary();
    }

    //<termUnary>  ::=  <sign_op> <element> | <element>
    protected Expr unary() {
        if (match(NOT, MINUS, PLUS)) {
            Token operator = previous();
            //Expr right = unary();
            Expr right = parseElement();
            return new Expr.Unary(operator, right);
        }

        return parseElement();
    }

    // <element> ::= "(" <expression> ")" | <identifier> | <type_value> | <function_call> | <array_access>
    protected Expr parseElement() {
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
            Token identifier = previous();
            TokenType currentType = current().getType();
            if(currentType == LEFT_PAREN) {
                return call();
            }
            else if(currentType == LEFT_BRACE) {
                return parseArrayAccess();
            }
            else {
                return new Expr.Variable(identifier);
            }
        }

        if (match(VARIABLES_OF_TYPE)) {
            return new Expr.Literal(previous().getLiteral());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = parseExpression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(current(), "Expect expression.");
    }

    // TODO Identifier to Expression
    private Expr parseArrayAccess() {
        Token identifier = previous();
        consume(LEFT_BRACE);
        Expr expression = parseExpression();
        consume(RIGHT_BRACE);
        return new Expr.ArrayAccess(identifier, expression);
    }

    // <function_call> ::= <identifier> "(" <argument_list> ")" | <identifier> "(" ")"
    protected Expr call() {
        Token identifier = previous();
        consume(LEFT_PAREN);

        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                arguments.add(parseExpression());
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expr.Call(identifier, arguments);
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

    private TokenType peekType() {
        if(!isAtEnd()) {
            return tokens.get(offset + 1).getType();
        }
        return EOF;
    }
}
