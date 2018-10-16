package com.jole;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        runFile(args[0]);
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        String fileName = Paths.get(path).getFileName().toString();
        run(new String(bytes, Charset.defaultCharset()), fileName);
    }

    private static void run(String source, String fileName) {
        Scanner scanner = new Scanner(source);
        ScannerResults scannerResults = scanner.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        List<LexerError> errors = scannerResults.getErrors();

        String tableFormat = "%4s|%4s|%-15s|%-50s%n";
        System.out.printf(tableFormat, "ID", "LN", "TYPE", "VALUE");
        System.out.printf(tableFormat, "----", "----", "---------------", "--------------------------------------------------");
        for (Token token : tokens) {
            Object value = token.getLiteral() == null ? "" : token.getLiteral();
            System.out.printf(tableFormat, tokens.indexOf(token), token.getLine(), token.getType(), value);
        }

        if(scannerResults.hasErrors()) {
            String errorColor = (char)27 + "[31m";
            for(LexerError error: errors) {
                System.out.printf("%sERROR at file '%s':line %s with message: %s%n", errorColor, fileName, error.getLine(), error.getErrorMessage());
            }
        }
    }
}
