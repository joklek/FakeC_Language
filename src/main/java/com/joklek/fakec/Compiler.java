package com.joklek.fakec;

import com.joklek.fakec.lexing.Lexer;
import com.joklek.fakec.parsing.Parser;
import com.joklek.fakec.tokens.Token;

import java.util.List;
import java.util.Map;

public class Compiler {

    public static void main(String[] args) {
        String filename = args[0];
        Lexer lexer = new Lexer();
        Map<String, List<Token>> tokensForFile = lexer.lexFile(filename);

        Parser parser = new Parser(tokensForFile.get(filename));
        parser.parseAll();
    }
}
