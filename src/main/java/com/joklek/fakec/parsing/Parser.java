package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.ast.Expr;
import com.joklek.fakec.parsing.error.ParserError;
import com.joklek.fakec.parsing.nodes.*;
import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;

import java.util.ArrayList;
import java.util.List;

import static com.joklek.fakec.tokens.TokenType.*;

public class Parser {

    private List<Token> tokens;
    private List<ParserError> errors;
    private int offset;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.errors = new ArrayList<>();
        this.offset = 0;
    }

    public Expr parse() {
        try {
            return expression();
        } catch (ParserError error) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(NOT_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = addition();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr addition() {
        Expr expr = multiplication();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr multiplication() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(NOT, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(NULL)) {
            return new Expr.Literal(null);
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

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
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
