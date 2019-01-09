package com.joklek.fakec.parsing;

import com.joklek.fakec.codegen.Label;
import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.ast.IExpr;
import com.joklek.fakec.parsing.ast.IStmt;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.error.ParserError;
import com.joklek.fakec.parsing.types.data.DataType;
import com.joklek.fakec.parsing.types.data.TypeConverter;
import com.joklek.fakec.parsing.types.operation.OperationConverter;
import com.joklek.fakec.parsing.types.operation.OperationType;
import com.joklek.fakec.parsing.types.operation.OperatorToken;
import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static com.joklek.fakec.tokens.TokenType.*;

@SuppressWarnings({"WeakerAccess", "squid:CommentedOutCodeLine"})
public class Parser {

    private List<Token> tokens;
    private List<ParserError> errors;
    private int offset;

    private final OperationConverter operationConverter;
    private final TypeConverter typeConverter;

    private static final TokenType[] FUNCTION_TYPES = {STRING_TYPE, FLOAT_TYPE, CHAR_TYPE, INT_TYPE, BOOL_TYPE, VOID_TYPE};
    private static final TokenType[] VARIABLE_TYPES = {STRING_TYPE, FLOAT_TYPE, CHAR_TYPE, INT_TYPE, BOOL_TYPE};
    private static final TokenType[] VARIABLES_OF_TYPE = {STRING, FLOAT, CHAR, INTEGER};

    public Parser(List<Token> tokens, OperationConverter operationConverter, TypeConverter typeConverter) {
        this.tokens = tokens;
        this.operationConverter = operationConverter;
        this.typeConverter = typeConverter;
        this.errors = new ArrayList<>();
        this.offset = 0;
    }

    // <program> ::= {<function>}<EOF>
    public ParserResults parseProgram() {
        List<Stmt.Function> functions = new ArrayList<>();
        while(current().getType() != EOF) {
            try{
                functions.add(parseFunction());
            }
            catch (ParserError e) {
                errors.add(e);
                synchronize();
            }
        }
        Stmt.Program program = new Stmt.Program(functions);
        return new ParserResults(program, errors);
    }

    // <fn_type_specifier> ["[""]"] <identifier> <fn_params> <block>
    protected Stmt.Function parseFunction() {
        Token lexerType = consume(FUNCTION_TYPES, "Functions should start with type");
        DataType type = typeConverter.convertToken(lexerType);
        if(Arrays.asList(VARIABLE_TYPES).contains(lexerType.getType()) && match(LEFT_BRACE)) {
            consume(RIGHT_BRACE, "Array type functions should not have anything between type braces");
            // TODO: Implement array methods
            //   type should be set by a parseType method, which should analyze the array part also.
            //   Array and normal types should be somehow linked, so having simple enums is not viable
            //   what if 2d array?
            //   AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAaa
        }
        Token name = consume(IDENTIFIER, "Expect function name.");

        List<Pair<Token, DataType>> parameters = parseParams();

        if(!check(CURLY_LEFT) ){
            throw error(current(), "Expect '{' before function body.");
        }

        Stmt.Block body = parseBlock();
        return new Stmt.Function(type, name, parameters, body);
    }

    // <fn_params> ::= "(" [<parameter> {"," <parameter>}] ")"
    //   <parameter> ::= <variable_type_specifier> <identifier>
    private List<Pair<Token, DataType>> parseParams() {
        consume(LEFT_PAREN, "Expect '(' after function name.");
        List<Pair<Token, DataType>>  parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                Token lexerParamType = consume(VARIABLE_TYPES, "Parameter should start with type");
                DataType dataType = typeConverter.convertToken(lexerParamType); // todo check if null
                if(match(LEFT_BRACE)) {
                    consume(RIGHT_BRACE, "Array type functions should not have anything between type braces");
                    // TODO: Implement array methods
                }
                Token parameterName = consume(IDENTIFIER, "Expect parameter name.");
                parameters.add(Pair.of(parameterName, dataType));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after function parameters.");
        return parameters;
    }

    // TODO can handle block nesting?
    // <block> ::= "{" {statement} "}"
    protected Stmt.Block parseBlock() {
        consume(CURLY_LEFT, "Expect '{' for block start.");
        List<IStmt> statements = new ArrayList<>();

        while (!check(CURLY_RIGHT) && !isAtEnd()) {
            try {
                statements.addAll(parseStatement());
            }
            catch (ParserError e) {
                errors.add(e);
                synchronize();
            }
        }

        consume(CURLY_RIGHT, "Expect '}' after block.");
        return new Stmt.Block(statements);
    }

    // <statement>  ::= <expression> ";" | <exit_keyword> ";"| "return" <expression> ";"| <assigment_statement> ";"| <var_declaration> ";"| <io_statement> ";" | <while_statement> | <for_statement> | <if_statements>
    protected List<IStmt> parseStatement() {
        Token previous = current();
        TokenType type = previous.getType();

        switch (type) {
            case RETURN:
                return Arrays.asList(parseReturnStmt());
            case BREAK:
                advance();
                consume(SEMICOLON, "Unclosed continue statement, semicolon is missing");
                return Arrays.asList(new Stmt.Break(previous));
            case CONTINUE:
                advance();
                consume(SEMICOLON, "Unclosed continue statement, semicolon is missing");
                return Arrays.asList(new Stmt.Continue(previous));
            case WHILE:
                return Arrays.asList(parseWhile());
            case FOR:
                return Arrays.asList(parseForStmt());
            case IF:
                return Arrays.asList(parseIfStmt());
            case INPUT:
                return Arrays.asList(parseInput());
            case OUTPUT:
                return Arrays.asList(parseOutput());
            default:
                if(Arrays.asList(VARIABLE_TYPES).contains(type)) {
                    return parseVarDecStmt();
                }
                return Arrays.asList(parseExprStatement());
        }
    }

    // <atomic_declaration> ::= <type_atomic_specifier> <identifier> ["=" <expression>] {, <identifier> ["=" <expression>]}
    protected List<IStmt> parseVarDecStmt() {
        Token tokenType = consume(VARIABLE_TYPES, "Expect variable type.");
        DataType type = typeConverter.convertToken(tokenType);
        if(check(LEFT_BRACE)) {
            // should use new parseType method, and if return value is an array, then should parse array
            return Arrays.asList(parseArrayDecStmt(type));
        }

        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = parseExpression();
        }

        List<IStmt> declarations = new ArrayList<>();
        IStmt declaration = new Stmt.Var(type, name, initializer);
        declarations.add(declaration);

        while(match(COMMA)) {
            name = consume(IDENTIFIER, "Expect variable name.");
            initializer = null;
            if (match(EQUAL)) {
                initializer = parseExpression();
            }
            declarations.add(new Stmt.Var(type, name, initializer));
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return declarations;
    }

    // <array_declaration>  ::= <type_atomic_specifier> "[" "]" <identifier> ("["<expression>"]" | "=" <expression>)
    private IStmt parseArrayDecStmt(DataType type) {
        consume(LEFT_BRACE);
        consume(RIGHT_BRACE, "Right brace should follow left brace in array declaration.");
        Token name = consume(IDENTIFIER, "Expect variable name.");

        IExpr initializer = parseArrayDeclarator(name);
        IStmt declaration = new Stmt.Array(type, name, initializer);

        if(check(COMMA)) {
            List<IStmt> declarations = new ArrayList<>();
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

    protected IStmt parseReturnStmt() {
        Token keyword = consume(RETURN);

        Expr value = null;
        boolean hasValue = !check(SEMICOLON);
        if (hasValue) {
            value = parseExpression();
        }

        consume(SEMICOLON, "Expect ';' after return statement.");
        return new Stmt.Return(keyword, value, hasValue);
    }

    protected IStmt parseForStmt() {
        consume(FOR);
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        List<IStmt> initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (Arrays.asList(VARIABLE_TYPES).contains(current().getType())) {
            initializer = parseVarDecStmt();
        } else {
            initializer = Collections.singletonList(parseExprStatement());
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
            List<IStmt> stmtList = new ArrayList<>(body.getStatements());
            stmtList.add(new Stmt.Expression(increment));
            body = new Stmt.Block(stmtList);
        }

        // if condition
        if (condition == null) {
            condition = new Expr.Literal(true);
            condition.setType(DataType.BOOL);
        }
        Stmt.While whileLoop = new Stmt.While(condition, body);

        // Adds initializer before while loop
        if (initializer != null) {
            List<IStmt> initStatements = new ArrayList<>(initializer);
            initStatements.add(whileLoop);
            body = new Stmt.Block(initStatements);
        }

        return body;
    }

    protected IStmt parseWhile() {
        consume(WHILE);
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = parseExpression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt.Block body = parseBlock();

        return new Stmt.While(condition, body);
    }

    protected IStmt parseIfStmt() {
        consume(IF);
        List<Pair<IExpr, Stmt.Block>> branches = new ArrayList<>();

        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = parseExpression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt.Block thenBranch = parseBlock();
        branches.add(Pair.of(condition, thenBranch));

        Stmt.Block elseBranch = null;

        while (check(ELSE) && peekType() == IF) {
            consume(ELSE);
            consume(IF);
            consume(LEFT_PAREN, "Expect '(' after 'if'.");
            condition = parseExpression();
            consume(RIGHT_PAREN, "Expect ')' after if condition.");
            thenBranch = parseBlock();
            branches.add(Pair.of(condition, thenBranch));
        }
        if (match(ELSE)) {
            elseBranch = parseBlock();
        }

        //return new Stmt.If(condition, thenBranch, elseBranch);

        return new Stmt.If(branches, elseBranch);
    }

    // <output_statement> ::= "output" "<<" <argument_list>
    protected IStmt parseOutput() {
        consume(OUTPUT);
        consume(OUTPUT_SIGN, "Output sign << should follow output keyword");
        List<IExpr> printExpressions = new ArrayList<>();
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

    protected IStmt parseExprStatement() {
        Expr expr = parseExpression();
         consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    // <expression> ::= <term4> {<or_op> <term4>}
    protected Expr parseExpression() {
        return parseAssignment();
    }


    // TOOD FIX BNF
    protected Expr parseAssignment() {
        Expr expr = parseOr();

        if (match(EQUAL, PLUS_EQUAL, MINUS_EQUAL, MUL_EQUAL, DIV_EQUAL, MOD_EQUAL)) {
            Token equals = previous();
            Expr value = parseAssignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).getName();
                return new Expr.Assign(name, value);
            }
            else if (expr instanceof Expr.ArrayAccess) {
                Token name = ((Expr.ArrayAccess)expr).getArray();
                return new Expr.Assign(name, value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    // <expression> ::= <term4> {<or_op> <term4>}
    protected Expr parseOr() {
        Expr expr = parseAnd();

        while (match(OR)) {
            Token lexerToken = previous();
            OperationType operator = operationConverter.convertToken(lexerToken);
            Expr right = parseAnd();
            expr = new Expr.Binary(expr, new OperatorToken(operator, lexerToken.getLine()), right);
        }

        return expr;
    }

    // <term4> ::= <term3> {<and_op> <term3>}
    protected Expr parseAnd() {
        Expr expr = parseEquality();

        while (match(AND)) {
            Token lexerToken = previous();
            OperationType operator = operationConverter.convertToken(lexerToken);
            Expr right = parseEquality();
            expr = new Expr.Binary(expr, new OperatorToken(operator, lexerToken.getLine()), right);
        }

        return expr;
    }

    // <term3> ::= <term2> { <equality_op> <term2> }
    protected Expr parseEquality() {
        Expr expr = parseComparison();

        while (match(NOT_EQUAL, EQUAL_EQUAL)) {
            Token lexerToken = previous();
            OperationType operator = operationConverter.convertToken(lexerToken);
            Expr right = parseComparison();
            expr = new Expr.Binary(expr, new OperatorToken(operator, lexerToken.getLine()), right);
        }

        return expr;
    }

    // <term2> ::= <term1> { <comparison_op> <term1> }
    protected Expr parseComparison() {
        Expr expr = parseAddition();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token lexerToken = previous();
            OperationType operator = operationConverter.convertToken(lexerToken);
            Expr right = parseAddition();
            expr = new Expr.Binary(expr, new OperatorToken(operator, lexerToken.getLine()), right);
        }

        return expr;
    }

    // <term1> ::= <term0> {<sign_op> <term0>}
    protected Expr parseAddition() {
        Expr expr = parseMultiplication();

        while (match(MINUS, PLUS)) {
            Token lexerToken = previous();
            OperationType operator = operationConverter.convertToken(lexerToken);
            Expr right = parseMultiplication();
            expr = new Expr.Binary(expr, new OperatorToken(operator, lexerToken.getLine()), right);
        }

        return expr;
    }

    // <term0> ::= <term_postfix> {<mul_div_op> <term_postfix>}
    protected Expr parseMultiplication() {
        //Expr expr = parsePostfixExpr();
        Expr expr = parsePostfixExpr();

        while (match(SLASH, STAR, MOD)) {
            Token lexerToken = previous();
            OperationType operator = operationConverter.convertToken(lexerToken);
            Expr right = parsePostfixExpr();
            expr = new Expr.Binary(expr, new OperatorToken(operator, lexerToken.getLine()), right);
        }

        return expr;
    }

    // TODO cleanup
    //<term_postfix> ::= <expression> <inc_dec_op> | <prefix_term>
    protected Expr parsePostfixExpr() {
        Expr expression = parsePrefixExpr();
        Token lexerToken = previous();
        if(expression instanceof Expr.Unary) {
            OperationType operator = ((Expr.Unary) expression).getOperator().getType();

            if(operator != OperationType.INC_PRE && operator != OperationType.DEC_PRE && match(INC, DEC)) {
                OperationType type = lexerToken.getType() == INC ? OperationType.INC_POST : OperationType.DEC_POST;

                return new Expr.Unary(new OperatorToken(type, lexerToken.getLine()), expression);
            }
        }
        if(match(INC, DEC)) {
            OperationType type = previous().getType() == INC ? OperationType.INC_POST : OperationType.DEC_POST;
            return new Expr.Unary(new OperatorToken(type, lexerToken.getLine()), expression);
        }
        return expression;
    }

    // ++i ::=
    private Expr parsePrefixExpr() {
        if(match(INC, DEC)) {
            Token lexerToken = previous();
            OperationType type = lexerToken.getType() == INC ? OperationType.INC_PRE : OperationType.DEC_PRE;
            return new Expr.Unary(new OperatorToken(type, lexerToken.getLine()), parseElement());
        }
        return parseNotExpr();
    }

    private Expr parseNotExpr() {
        Expr expr = null;
        while(match(NOT)) {
            Token lexerToken = previous();
            OperationType operator = operationConverter.convertToken(lexerToken);
            expr = new Expr.Unary(new OperatorToken(operator, lexerToken.getLine()), parseNotExpr());
        }
        if (expr != null) {
            return expr;
        }
        return parseUnary();
    }

    //<termUnary>  ::=  <sign_op> <element> | <element>
    protected Expr parseUnary() {
        if (match(NOT, MINUS, PLUS)) {
            Token lexerToken = previous();
            OperationType operator = operationConverter.convertToken(lexerToken);
            Expr right = parseElement();
            return new Expr.Unary(new OperatorToken(operator, lexerToken.getLine()), right);
        }

        return parseElement();
    }

    // <element> ::= "(" <expression> ")" | <identifier> | <type_value> | <function_call> | <array_access>
    protected Expr parseElement() {
        if (match(FALSE)) {
            Expr.Literal literal = new Expr.Literal(false);
            literal.setType(DataType.BOOL);
            return literal;
        }
        if (match(TRUE)) {
            Expr.Literal literal = new Expr.Literal(true);
            literal.setType(DataType.BOOL);
            return literal;
        }
        if (match(NULL)) {
            Expr.Literal literal = new Expr.Literal(null);
            literal.setType(DataType.NULL);
            return literal;
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
        if (match(RANDOM)) {
            consume(LEFT_BRACE, "[ should follow the random statement");
            Expr minInclusive = parseExpression();
            Token comma = consume(COMMA, "Comma should separate random restriction");
            Expr maxInclusive = parseExpression();
            consume(RIGHT_BRACE, "] should be at the end of a random expression");
            return new Expr.Random(minInclusive, maxInclusive, comma);
        }

        if (match(VARIABLES_OF_TYPE)) {
            DataType type = typeConverter.convertToken(previous());
            Expr.Literal literal = new Expr.Literal(previous().getLiteral());
            literal.setType(type);
            return literal;
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
        return new ParserError(message, token);
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
                case WHILE:
                case INPUT:
                case OUTPUT:
                case RETURN:
                case BREAK:
                case CONTINUE:
                    return;
                case IF:
                    if(previous().getType() != ELSE) {
                        return;
                    }
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
