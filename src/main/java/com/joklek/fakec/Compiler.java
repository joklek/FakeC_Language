package com.joklek.fakec;

import com.joklek.fakec.lexing.Lexer;
import com.joklek.fakec.parsing.AstPrinter;
import com.joklek.fakec.parsing.Parser;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;

import java.util.List;
import java.util.Map;

public class Compiler {

    static boolean hadError = false;

    public static void main(String[] args) {
        String filename = args[0];
        Lexer lexer = new Lexer();
        Map<String, List<Token>> tokensForFile = lexer.lexFile(filename);

        Parser parser = new Parser(tokensForFile.get(filename));
        Stmt.Program program = parser.parseProgram().getRootNode();
        System.out.println(new AstPrinter().print(program));
        /*List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) return;

        System.out.println(new AstPrinter().print(statements.get(0)));*/
    }

    public static void error(Token token, String message) {
        if (token.getType() == TokenType.EOF) {
            report(token.getLine(), " at end", message);
        } else {
            report(token.getLine(), " at '" + token.getLexeme() + "'", message);
        }
    }

    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
