package com.jole.fakec.lexing.utils;

import com.jole.fakec.tokens.TokenType;

import java.util.HashMap;
import java.util.Map;

public class ReservedKeywordUtils {

    private Map<String, TokenType> keywords = new HashMap<>();

    public ReservedKeywordUtils() {
        keywords.put("AND", TokenType.AND);
        keywords.put("OR", TokenType.OR);
        keywords.put("return", TokenType.RETURN);
        keywords.put("break", TokenType.BREAK);
        keywords.put("continue", TokenType.CONTINUE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("null", TokenType.NULL);
        keywords.put("float", TokenType.FLOAT_TYPE);
        keywords.put("int", TokenType.INT_TYPE);
        keywords.put("char", TokenType.CHAR_TYPE);
        keywords.put("bool", TokenType.BOOL_TYPE);
        keywords.put("string", TokenType.STRING_TYPE);
        keywords.put("void", TokenType.VOID_TYPE);
        keywords.put("input", TokenType.INPUT);
        keywords.put("output", TokenType.OUTPUT);
    }

    /**
     * @param identifier identifier for which to get the token type
     * @return TokenType of given identifier or {@code null} if identifier is not a reserved word
     */
    public TokenType getTokenType(String identifier) {
        return keywords.get(identifier);
    }
}
