package com.joklek.fakec;

import com.joklek.fakec.lexing.Lexer;
import com.joklek.fakec.parsing.*;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.error.ParserError;
import com.joklek.fakec.parsing.error.ScopeError;
import com.joklek.fakec.parsing.types.OperationConverter;
import com.joklek.fakec.parsing.types.TypeConverter;
import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;

import java.util.List;
import java.util.Map;

public class Compiler {

    public static void main(String[] args) {
        String filename = args[0];
        Lexer lexer = new Lexer();
        Map<String, List<Token>> tokensForFile = lexer.lexFile(filename);

        OperationConverter operationConverter = new OperationConverter();
        TypeConverter typeConverter = new TypeConverter();
        // TODO work with more files than one
        Parser parser = new Parser(tokensForFile.get(filename), operationConverter, typeConverter);
        ParserResults results = parser.parseProgram();
        List<ParserError> errors = results.getErrors();
        Stmt.Program program = results.getRootNode();
        System.out.println(new AstPrinter().print(program));

        for(ParserError error: errors) {
            error(error, filename);
        }

        ScopeResolver scopeResolver = new ScopeResolver();
        List<ScopeError> scopeErrors = scopeResolver.resolveNames(program, new Scope());

        for(ScopeError error: scopeErrors) {
            error(error, filename);
        }
    }

    private static void error(ScopeError error, String filename) {
        Token token = error.getErroneousName();
        String message = error.getErrorMessage();
        report(token.getLine(), filename," at '" + token.getLexeme() + "'", message); // + " : " + error.getErroneousName().getLexeme());
    }

    private static void error(ParserError error, String filename) {
        Token token = error.getToken();
        String message = error.getErrorMessage();
        if (token.getType() == TokenType.EOF) {
            report(token.getLine(), filename," at end", message);
        } else {
            report(token.getLine(), filename," at '" + token.getLexeme() + "'", message);
        }
    }

    private static void report(int line, String filename, String where, String message) {
        System.err.printf("%s:%d:error:%s%n", filename, line, "Error" + where + ": " + message);
    }
}
