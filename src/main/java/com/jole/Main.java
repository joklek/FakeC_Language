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
        run(new String(bytes, Charset.defaultCharset()));
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        String tableFormat = "%4s|%4s|%-15s|%-50s%n";
        System.out.printf(tableFormat, "ID", "LN", "TYPE", "VALUE");
        System.out.printf(tableFormat, "----", "----", "---------------", "--------------------------------------------------");
        for (Token token : tokens) {
            Object value = token.getLiteral() == null ? "" : token.getLiteral();
            System.out.printf(tableFormat, tokens.indexOf(token), token.getLine(), token.getType(), value);
        }
    }
}
