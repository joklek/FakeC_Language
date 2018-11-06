package com.jole.fakec.lexing;

import com.jole.fakec.lexing.error.LexerError;
import com.jole.fakec.lexing.sourceproviders.CodeCollector;
import com.jole.fakec.lexing.sourceproviders.SourceFromFile;
import com.jole.fakec.tokens.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {

    public Map<String, List<Token>> lexFile(String mainFile) {
        CodeCollector codeCollector = new CodeCollector(new SourceFromFile());

        Map<String, List<LexerError>> errorsForFiles = new HashMap<>();
        Map<String, List<Token>> tokensForFiles = new HashMap<>();

        codeCollector.getAllRelatedCode(mainFile).forEach((fileName, source) -> {
            ScannerResults results = run(fileName, source);
            List<LexerError> errors = results.getErrors();
            List<Token> tokens = results.getTokens();
            errorsForFiles.put(fileName, errors);
            tokensForFiles.put(fileName, tokens);
        });

        errorsForFiles.forEach(this::showErrors);
        return tokensForFiles;
    }

    private ScannerResults run(String fileName, String source) {
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
        return scannerResults;
    }

    private void showErrors(String fileName, List<LexerError> errors) {
        if(!errors.isEmpty()) {
            for(LexerError error: errors) {
                System.err.printf("%s:%d:error:%s%n", fileName, error.getLine(), error.getErrorMessage());
            }
        }
    }
}
