package com.joklek.fakec;

import com.joklek.fakec.lexing.Lexer;
import com.joklek.fakec.parsing.AstPrinter;
import com.joklek.fakec.parsing.Parser;
import com.joklek.fakec.parsing.ParserResults;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.error.ParserError;
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
        Parser parser = new Parser(tokensForFile.get(filename), operationConverter, typeConverter);
        ParserResults results = parser.parseProgram();
        List<ParserError> errors = results.getErrors();
        Stmt.Program program = results.getRootNode();
        System.out.println(new AstPrinter().print(program));

        for(ParserError error: errors) {
            error(error.getToken(), error.getErrorMessage(), filename);
        }
    }

    private static void error(Token token, String message, String filename) {
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
