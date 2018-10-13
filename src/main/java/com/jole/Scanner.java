package com.jole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jole.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens;
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
        this.tokens = new ArrayList<>();
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '"': lexString(); break;

            case  '(': addToken(LEFT_PAREN); break;
            case  ')': addToken(RIGHT_PAREN); break;
            case  '{': addToken(CURLY_LEFT); break;
            case  '}': addToken(CURLY_RIGHT); break;
            case  '[': addToken(LEFT_BRACE); break;
            case  ']': addToken(RIGHT_BRACE); break;
            case  ',': addToken(COMMA); break;
            case  ';': addToken(SEMICOLON); break;

            case  '+': addToken(match('=') ? PLUS_EQUAL : PLUS); break;
            case  '-': addToken(match('=') ? MINUS_EQUAL : MINUS); break;
            case  '!': addToken(match('=') ? NOT_EQUAL : NOT); break;
            case  '*': addToken(match('=') ? MUL_EQUAL : STAR); break;
            case  '%': addToken(match('=') ? MOD_EQUAL : PERCENT); break;
            case  '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case  '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case  '<': addToken(match('=') ? LESS_EQUAL : LESS); break;

            case  '/':
                if(match('/')) {
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                }
                if(match('=')) {
                    addToken(DIV_EQUAL);
                }
                else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\t':
            case '\r':
                // do nothing
                break;
            case '\n':
                line++;
                break;
            default:
                if(isDigit(c) || (c == '.' && isDigit(peekNext())) ) {
                    lexNumber();
                }
                else {
                    // TODO ERROR
                }
                break;
        }
    }

    private void lexNumber() {
        boolean isFloat = false;

        if(source.charAt(current - 1) != '.') {
            collectNumbers();
        }
        // if dot is consumed, unconsume for simplified lexing
        else {
            isFloat = true;  // this flag is optionally set here for clarity
            current--;
        }

        if (peek() == '.') {
            isFloat = true;
            advance();
            collectNumbers();
        }
        if (peek() == 'e') {
            isFloat = true;
            advance();
            collectNumbers();
        }

        if(isFloat) {
            String value = source.substring(start, current);
            Double unescapedValue = Double.parseDouble(value);
            addToken(FLOAT, unescapedValue);
        }
        else {
            String value = source.substring(start, current);
            Integer unescapedValue = Integer.parseInt(value);
            addToken(INTEGER, unescapedValue);
        }
    }

    private void collectNumbers() {
        while (isDigit(peek())) {
            advance();
        }
    }

    private void lexString() {
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') {
                line++;
            }
            advance();
        }
        if(isAtEnd()) {
            // TODO: error unterminated string
            return;
        }

        // for closing "
        advance();

        String value = source.substring(start + 1, current - 1);
        String unescapedValue = unescapeString(value);
        addToken(STRING, unescapedValue);
    }

    private String unescapeString(final String value) {
        String unescaped = value;
        Map<String, String> escapedChars = new HashMap<>();
        escapedChars.put("\\n", "\n");
        escapedChars.put("\\r", "\r");
        escapedChars.put("\\t", "\t");
        escapedChars.put("\\\"", "\"");

        for(Map.Entry<String, String> entry : escapedChars.entrySet()) {
            unescaped = unescaped.replace(entry.getKey(), entry.getValue());
        }
        return unescaped;
    }

    /**
     * peeks a character
     * @return character in front
     */
    private char peek() {
        if (isAtEnd()) {
            return '\n';
        }
        return source.charAt(current);
    }

    /**
     * peeks following character
     * @return character in front
     */
    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    /**
     * @param charToMatch character to match
     * @return true if current char is charToMatch
     */
    private boolean match(char charToMatch) {
        if(isAtEnd()) {
            return false;
        }
        if(source.charAt(current) != charToMatch) {
            return false;
        }
        current++;
        return true;
    }

    /**
     * steps one character
     * @return previous character
     */
    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType tokenType) {
        addToken(tokenType, null);
    }

    private void addToken(TokenType tokenType, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(tokenType, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
