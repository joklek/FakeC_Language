package com.jole.fakec;

import com.jole.fakec.tokens.Token;

import java.util.List;

public class ScannerResults {
    private final List<Token> tokens;
    private final List<LexerError> errors;

    public ScannerResults(List<Token> tokens, List<LexerError> errors) {
        this.tokens = tokens;
        this.errors = errors;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<LexerError> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
