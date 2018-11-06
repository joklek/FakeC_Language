package com.jole.fakec.parsing;

import com.jole.fakec.parsing.error.ParserError;
import com.jole.fakec.parsing.nodes.*;
import com.jole.fakec.tokens.Token;
import com.jole.fakec.tokens.TokenType;

import java.util.ArrayList;
import java.util.List;

import static com.jole.fakec.tokens.TokenType.*;

public class Parser {

    private List<Token> tokens;
    private List<ParserError> errors;
    private int offset;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.errors = new ArrayList<>();
        this.offset = 0;
    }

    public ParserResults parseAll() {
        Program program = parseProgram();
        return new ParserResults(program, errors);
    }

    // {<function>}<EOF>
    private Program parseProgram() {
        Program program = new Program();
        while(!accept(EOF)) {
            DefFunc func = parseDefFn();
            program.add_func(func);
        }
        return program;
    }

    // <fn_type_specifier> <identifier> <fn_params> <block>
    private DefFunc parseDefFn() {
        Type type = parseType();
        String name = expect(IDENTIFIER).getLexeme();
        List<Param> params = parseParams();
        StmtBlock block = parseBlock();
        return new DefFunc(type, name, params, block);
    }

    // "{" {statement} "}"
    private StmtBlock parseBlock() {
        StmtBlock stmtBlock = new StmtBlock();
        expect(CURLY_LEFT);
        while(!accept(CURLY_RIGHT)) {
            Statement stmt = parseStmt();
            stmtBlock.addStmt(stmt);
        }
        return stmtBlock;
    }

    // <statement> ::= <single_statement> ";" | <control_statement>
    private Statement parseStmt() {
        Token current = current();
        Statement returnedStatement = null;
        switch (current.getType()) {
            case SEMICOLON: // TODO: Think about dangling semicolons
                break;
            case RETURN:
                if(peek(SEMICOLON))
                    accept(RETURN);
                    returnedStatement = new Statement.ReturnStmt();
                break;
            case BREAK:
                accept(BREAK);
                returnedStatement = new Statement.BreakStmt();
                break;
            case CONTINUE:
                accept(CONTINUE);
                returnedStatement = new Statement.ContinueStmt();
                break;
            case WHILE:
            case FOR:
            case IF:
                returnedStatement = parseControl();
                break;
            case FLOAT_TYPE:
            case INT_TYPE:
            case CHAR_TYPE:
            case BOOL_TYPE:
            case STRING_TYPE:
                returnedStatement = parseStmtDeclaration();
                break;
            case INPUT:
                returnedStatement = parseStmtInput();
                break;
            case OUTPUT:
                returnedStatement = parseStmtOutput();
                break;
            case IDENTIFIER:
                if (peek(EQUAL) || peek(PLUS_EQUAL) ||  peek(MINUS_EQUAL) ||
                        peek(MUL_EQUAL) || peek(DIV_EQUAL) || peek(NOT_EQUAL) ) {
                    returnedStatement = parseAsignment();
                    break;
                }
                returnedStatement = new Statement.ExpressionStatement(parseExpression());
                break;
            default:
                returnedStatement = new Statement.ExpressionStatement(parseExpression());
        }
        if(!(returnedStatement instanceof ControlStatement)) {
            expect(SEMICOLON);
        }
        return returnedStatement;
    }

    private Statement parseAsignment() {
        return null;
    }

    private Statement parseStmtOutput() {
        return null;
    }

    private Statement parseControl() {
        return null;
    }

    private Statement parseStmtInput() {
        return null;
    }

    // TODO Arrays
    // <type_atomic_specifier> <atomic_declarator> {"," <atomic_declarator>}
    // ne list
    private Statement.Declarations parseStmtDeclaration() {
        List<Statement.Declaration> declarations = new ArrayList<>();
        Type type = parseType();
        String name = expect(IDENTIFIER).getLexeme();
        Expression value = null;
        if (accept(EQUAL)) {
            value = parseExpression();
        }
        declarations.add(new Statement.Declaration(type, name, value));
        while(accept(COMMA)) {
            name = expect(IDENTIFIER).getLexeme();
            if (accept(EQUAL)) {
                value = parseExpression();
            }
            else {
                value = null;
            }
            declarations.add(new Statement.Declaration(type, name, value));
        }
        return new Statement.Declarations(type, declarations);
    }

    private Expression parseExpression() {
        accept(INTEGER);
        return new Expression.ExprVar();
    }

    // "(" [<parameter> {"," <parameter>}] ")"
    private List<Param> parseParams() {
        List<Param> params = new ArrayList<>();
        expect(LEFT_PAREN);
        if(current().getType() != RIGHT_PAREN) {
            params.add(parseParam());
        }
        while (!accept(RIGHT_PAREN)) {
            expect(COMMA);
            params.add(parseParam());
        }
        return params;
    }

    // <variable_type_specifier> <identifier>
    private Param parseParam() {
        String name = expect(IDENTIFIER).getLexeme();
        Type type = parseType();
        return new Param(name, type);
    }

    // <type_atomic_specifier> | <type_array_specifier> // TODO array
    private Type parseType() {
        Token current = current();
        switch (current.getType()) {
            case FLOAT_TYPE:
                expect(FLOAT_TYPE);
                return new Type.TypeFloat();
            case INT_TYPE:
                expect(INT_TYPE);
                return new Type.TypeInt();
            case CHAR_TYPE:
                expect(CHAR_TYPE);
                return new Type.TypeChar();
            case BOOL_TYPE:
                expect(BOOL_TYPE);
                return new Type.TypeBool();
            case STRING_TYPE:
                expect(STRING_TYPE);
                return new Type.TypeString();
            case VOID_TYPE: // TODO remove void
                expect(VOID_TYPE);
                return new Type.TypeVoid();
            default:
                //return new ParserError(String.format("Incorrect type: '%s'", current.getType()), current.getLine());
                throw new IllegalStateException(String.format("Incorrect type: '%s' %d", current.getType(), current.getLine()));
        }
    }

    private Token current() {
        return tokens.get(offset);
    }

    private boolean peek(TokenType type) {
        Token current = current();
        return current.getType().equals(type);
    }

    private boolean accept(TokenType type) {
        Token current = current();
        if (current.getType().equals(type)) {
            offset++;
            return true;
        }
        return false;
    }

    private Token expect(TokenType type) {
        Token current = current();
        if (current.getType().equals(type)) {
            offset++;
            return current;
        }
        else {
            //return new ParserError(String.format("Expected '%s', but is '%s'", type, current.getType()), current.getLine());
            throw new IllegalStateException(String.format("Expected '%s', but is '%s'", type, current.getType()));
        }
    }
}
