package com.jole.fakec;

import com.jole.fakec.sourceproviders.CodeCollector;
import com.jole.fakec.sourceproviders.SourceFromFile;
import com.jole.fakec.tokens.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {

    public static void main(String[] args) {
        CodeCollector codeCollector = new CodeCollector(new SourceFromFile());

        Map<String, List<LexerError>> errorsForFiles = new HashMap<>();

        codeCollector.getAllRelatedCode(args[0]).forEach((fileName, source) -> {
            List<LexerError> errors = run(fileName, source);
            errorsForFiles.put(fileName, errors);
        });

        errorsForFiles.forEach(Lexer::showErrors);
    }

    private static List<LexerError> run(String fileName, String source) {
        Scanner scanner = new Scanner(source);
        ScannerResults scannerResults = scanner.scanTokens();
        List<Token> tokens = scannerResults.getTokens();

        String tableFormat = "%4s|%4s|%-15s|%-50s%n";
        System.out.println("Lexemas for file: " + fileName);
        System.out.printf(tableFormat, "ID", "LN", "TYPE", "VALUE");
        System.out.printf(tableFormat, "----", "----", "---------------", "--------------------------------------------------");
        for (Token token : tokens) {
            Object value = token.getLiteral() == null ? "" : token.getLiteral();
            System.out.printf(tableFormat, tokens.indexOf(token), token.getLine(), token.getType(), value);
        }
        System.out.println();
        return scannerResults.getErrors();
    }

    private static void showErrors(String fileName, List<LexerError> errors) {
        if(!errors.isEmpty()) {
            for(LexerError error: errors) {
                System.err.printf("%s:%d:error:%s%n", fileName, error.getLine(), error.getErrorMessage());
            }
        }
    }
}
