package com.jole.utils;

import com.jole.TokenType;

import java.util.HashMap;
import java.util.Map;

import static com.jole.TokenType.*;

public class ReservedKeywordUtils {

    private Map<String, TokenType> keywords = new HashMap<>();

    public ReservedKeywordUtils() {
        keywords.put("AND", AND);
        keywords.put("OR", OR);
        keywords.put("NOT", NOT);
        keywords.put("return", RETURN);
        keywords.put("break", BREAK);
        keywords.put("continue", CONTINUE);
        keywords.put("while", WHILE);
        keywords.put("for", FOR);
        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("true", TRUE);
        keywords.put("false", FALSE);
        keywords.put("null", NULL);
        keywords.put("float", FLOAT_TYPE);
        keywords.put("int", INT_TYPE);
        keywords.put("char", CHAR_TYPE);
        keywords.put("bool", BOOL_TYPE);
        keywords.put("string", STRING_TYPE);
        keywords.put("void", VOID_TYPE);
        keywords.put("input", INPUT);
        keywords.put("output", OUTPUT);
    }

    /**
     * @param identifier identifier for which to get the token type
     * @return TokenType of given identifier or {@code null} if identifier is not a reserved word
     */
    public TokenType getTokenType(String identifier) {
        return keywords.get(identifier);
    }
}
